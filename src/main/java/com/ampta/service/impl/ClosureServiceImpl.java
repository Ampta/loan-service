
package com.ampta.service.impl;

import com.ampta.dto.request.ClosureCreateRequest;
import com.ampta.dto.response.ClosureResponse;
import com.ampta.entity.ClosureRequest;
import com.ampta.entity.LoanAccount;
import com.ampta.repository.ClosureRepository;
import com.ampta.repository.ForeclosureRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.service.ClosureService;
import com.ampta.service.LoanPaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosureServiceImpl implements ClosureService {

    private final ClosureRepository repository;
    private final ForeclosureRepository foreclosureRepository;
    private final LoanAccountRepository accounts;
    private final LoanPaymentService loanPaymentService;

    private ClosureRequest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Request not found: " + id
                ));
    }

    private static void require(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Expected status " + expected + ", found " + actual
            );
        }
    }

    private static BigDecimal principal(LoanAccount account) {
        return BigDecimal.valueOf(
                account.getOutstandingPrincipal() == null
                        ? 0
                        : account.getOutstandingPrincipal()
        );
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public ClosureResponse create(ClosureCreateRequest input) {

        LoanAccount account = accounts
                .findByIdForUpdate(input.loanAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan account not found"
                ));

        if ("CLOSED".equalsIgnoreCase(account.getLoanStatus())
                || principal(account).signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Loan is closed or has no outstanding principal"
            );
        }

        if (repository.existsByLoanAccount_LoanAccountIdAndStatusIn(
                account.getLoanAccountId(),
                Arrays.asList("PENDING", "APPROVED", "PAID"))
                || foreclosureRepository
                .existsByLoanAccount_LoanAccountIdAndStatusIn(
                        account.getLoanAccountId(),
                        Arrays.asList("PENDING", "APPROVED", "PAID"))) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Loan already has a pending settlement request"
            );
        }

        BigDecimal amount = money(input.paymentAmount());

        if (amount.signum() <= 0
                || amount.compareTo(principal(account)) >= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Partial amount must be greater than zero and less than outstanding principal"
            );
        }

        ClosureRequest request = new ClosureRequest();
        request.setLoanAccount(account);
        request.setPaymentAmount(amount);

        return ClosureResponse.from(repository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureResponse get(Long id) {
        return ClosureResponse.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClosureResponse> all() {
        return repository.findAll()
                .stream()
                .map(ClosureResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClosureResponse> byLoan(Long loanId) {
        return repository
                .findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(loanId)
                .stream()
                .map(ClosureResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public ClosureResponse approve(Long id) {
        ClosureRequest request = find(id);

        require(request.getStatus(), "PENDING");

        request.setStatus("APPROVED");
        request.setReviewedAt(LocalDateTime.now());

        return ClosureResponse.from(request);
    }

    @Override
    @Transactional
    public ClosureResponse reject(Long id, String reason) {
        ClosureRequest request = find(id);

        require(request.getStatus(), "PENDING");

        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rejection reason required"
            );
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        request.setReviewedAt(LocalDateTime.now());

        return ClosureResponse.from(request);
    }

    @Override
    @Transactional
    public ClosureResponse pay(Long id, String reference) {
        ClosureRequest request = find(id);

        require(request.getStatus(), "APPROVED");

        if (reference == null || reference.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment reference required"
            );
        }

        if (repository.existsByPaymentReference(reference)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate payment reference"
            );
        }

        // Reference alone does not verify actual payment.
        // Call this workflow only after trusted payment confirmation.
        request.setPaymentReference(reference);
        request.setStatus("PAID");
        request.setPaidAt(LocalDateTime.now());

        return ClosureResponse.from(request);
    }

    @Override
    @Transactional
    public ClosureResponse complete(Long id) {
        ClosureRequest request = find(id);

        require(request.getStatus(), "PAID");

        LoanAccount account = accounts
                .findByIdForUpdate(
                        request.getLoanAccount().getLoanAccountId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan account not found"
                ));

        BigDecimal outstanding = money(principal(account));
        BigDecimal amount = request.getPaymentAmount();

        if (amount.compareTo(outstanding) >= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Partial payment must remain smaller than outstanding principal"
            );
        }

        BigDecimal remaining = outstanding.subtract(amount);

        account.setOutstandingPrincipal(remaining.doubleValue());

        BigDecimal totalPaid = BigDecimal.valueOf(
                account.getTotalPaidAmount() == null
                        ? 0
                        : account.getTotalPaidAmount()
        );

        account.setTotalPaidAmount(
                money(totalPaid.add(amount)).doubleValue()
        );

        // Common payment-recording method
        loanPaymentService.recordNonEmiPayment(
                account,
                amount.doubleValue(),
                "PARTIAL_PAYMENT",
                "EXTERNAL"
        );

        // Existing unpaid EMI schedules need re-amortization
        // according to lender policy.
        request.setStatus("COMPLETED");
        request.setCompletedAt(LocalDateTime.now());

        return ClosureResponse.from(request);
    }
}
