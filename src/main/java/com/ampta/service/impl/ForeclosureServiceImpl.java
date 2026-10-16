
package com.ampta.service.impl;

import com.ampta.dto.request.ForeclosureCreateRequest;
import com.ampta.dto.response.ForeclosureResponse;
import com.ampta.entity.*;
import com.ampta.repository.*;
import com.ampta.service.ForeclosureService;
import com.ampta.service.LoanPaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForeclosureServiceImpl implements ForeclosureService {

    private final ForeclosureRepository repository;
    private final ClosureRepository closureRepository;
    private final LoanAccountRepository accounts;
    private final EmiScheduleRepository schedules;
    private final LoanPaymentService loanPaymentService;

    private ForeclosureRequest find(Long id) {
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
    public ForeclosureResponse create(ForeclosureCreateRequest input) {
        LoanAccount account = accounts.findByIdForUpdate(
                input.loanAccountId()
        ).orElseThrow(() -> new ResponseStatusException(
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
                List.of("PENDING", "APPROVED", "PAID")
        ) || closureRepository.existsByLoanAccount_LoanAccountIdAndStatusIn(
                account.getLoanAccountId(),
                List.of("PENDING", "APPROVED", "PAID")
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Loan already has a pending settlement request"
            );
        }

        ForeclosureRequest request = new ForeclosureRequest();
        request.setLoanAccount(account);
        request.setSettlementAmount(money(principal(account)));

        return ForeclosureResponse.from(repository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public ForeclosureResponse get(Long id) {
        return ForeclosureResponse.from(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForeclosureResponse> all() {
        return repository.findAll()
                .stream()
                .map(ForeclosureResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForeclosureResponse> byLoan(Long loanId) {
        return repository
                .findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(loanId)
                .stream()
                .map(ForeclosureResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ForeclosureResponse approve(Long id) {
        ForeclosureRequest request = find(id);
        require(request.getStatus(), "PENDING");

        request.setStatus("APPROVED");
        request.setReviewedAt(LocalDateTime.now());

        return ForeclosureResponse.from(request);
    }

    @Override
    @Transactional
    public ForeclosureResponse reject(Long id, String reason) {
        ForeclosureRequest request = find(id);
        require(request.getStatus(), "PENDING");

        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rejection reason required"
            );
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        request.setReviewedAt(LocalDateTime.now());

        return ForeclosureResponse.from(request);
    }

    @Override
    @Transactional
    public ForeclosureResponse pay(Long id, String reference) {
        ForeclosureRequest request = find(id);
        require(request.getStatus(), "APPROVED");

        if (reference == null || reference.isBlank()) {
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

        // Records an externally confirmed payment reference.
        // This method does not verify a payment gateway transaction.
        request.setPaymentReference(reference);
        request.setStatus("PAID");
        request.setPaidAt(LocalDateTime.now());

        return ForeclosureResponse.from(request);
    }

    @Override
    @Transactional
    public ForeclosureResponse complete(Long id) {
        ForeclosureRequest request = find(id);
        require(request.getStatus(), "PAID");

        LoanAccount account = accounts.findByIdForUpdate(
                request.getLoanAccount().getLoanAccountId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Loan account not found"
        ));

        BigDecimal outstanding = money(principal(account));
        BigDecimal amount = request.getSettlementAmount();

        if (amount.compareTo(outstanding) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Principal changed since foreclosure was requested; settlement needs review"
            );
        }

        BigDecimal remaining = outstanding.subtract(amount);

        account.setOutstandingPrincipal(remaining.doubleValue());

        account.setTotalPaidAmount(
                money(
                        BigDecimal.valueOf(
                                account.getTotalPaidAmount() == null
                                        ? 0
                                        : account.getTotalPaidAmount()
                        ).add(amount)
                ).doubleValue()
        );

        account.setLoanStatus("CLOSED");

        // Common payment-recording logic
        loanPaymentService.recordNonEmiPayment(
                account,
                amount.doubleValue(),
                "FORECLOSURE_PAYMENT",
                "EXTERNAL"
        );

        for (EmiSchedule schedule :
                schedules.getEmiScheduleDetailsByLoanAccountId(
                        account.getLoanAccountId()
                )) {
            if ("PENDING".equalsIgnoreCase(schedule.getPaymentStatus())) {
                schedule.setPaymentStatus("CANCELLED");
                schedule.setCancellationReason("Loan foreclosed");
            }
        }

        request.setStatus("COMPLETED");
        request.setCompletedAt(LocalDateTime.now());

        return ForeclosureResponse.from(request);
    }
}
