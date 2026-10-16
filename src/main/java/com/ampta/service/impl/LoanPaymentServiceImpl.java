
package com.ampta.service.impl;

import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.response.LoanPaymentResponse;
import com.ampta.entity.EmiSchedule;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanPayment;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.EmiScheduleRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.LoanPaymentRepository;
import com.ampta.service.LoanPaymentService;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanPaymentServiceImpl implements LoanPaymentService {

    private final LoanPaymentRepository loanPaymentRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    @Override
    public LoanPaymentResponse makePayment(
            LoanPaymentRequest request) throws BadRequestException {

        // 1. Validate request
        if (request == null
                || request.getLoanAccountId() == null
                || request.getEmiScheduleId() == null
                || request.getPaymentAmount() == null) {

            throw new BadRequestException(
                    "Loan account ID, EMI schedule ID and payment amount are required");
        }

        // 2. Fetch loan account
        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found with id: "
                                + request.getLoanAccountId()));

        // 3. Fetch EMI schedule
        EmiSchedule schedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI schedule not found with id: "
                                + request.getEmiScheduleId()));

        // 4. Verify EMI belongs to the loan account
        if (schedule.getLoanAccount() == null
                || !schedule.getLoanAccount().getLoanAccountId()
                .equals(account.getLoanAccountId())) {

            throw new BadRequestException(
                    "EMI does not belong to this loan account");
        }

        // 5. Check EMI status
        if (!"PENDING".equalsIgnoreCase(schedule.getPaymentStatus())) {
            throw new BadRequestException(
                    "This EMI is not pending payment");
        }

        // 6. Validate payment amount
        double paymentAmount =
                roundToTwoDecimals(request.getPaymentAmount());

        Double emiAmount = schedule.getEmiAmount();

        if (paymentAmount <= 0
                || emiAmount == null
                || emiAmount <= 0) {

            throw new BadRequestException(
                    "Payment amount and EMI amount must be positive");
        }

        if (Math.abs(
                paymentAmount - roundToTwoDecimals(emiAmount)) > 0.001) {

            throw new BadRequestException(
                    "Invalid EMI payment amount. Expected: "
                            + roundToTwoDecimals(emiAmount));
        }

        // 7. Validate principal and outstanding balance
        Double principalValue = schedule.getPrincipalAmount();
        Double outstandingValue = account.getOutstandingPrincipal();

        if (principalValue == null || outstandingValue == null) {
            throw new BadRequestException(
                    "Principal amount or outstanding balance is missing");
        }

        double principalAmount = roundToTwoDecimals(principalValue);
        double outstanding = roundToTwoDecimals(outstandingValue);

        if (principalAmount < 0 || outstanding < 0) {
            throw new BadRequestException(
                    "Principal amount and outstanding balance cannot be negative");
        }

        if (principalAmount > outstanding + 0.001) {
            throw new BadRequestException(
                    "EMI principal cannot exceed outstanding principal");
        }

        // 8. Update outstanding principal
        double remaining = roundToTwoDecimals(
                Math.max(0.0, outstanding - principalAmount));

        account.setOutstandingPrincipal(remaining);

        // 9. Update total paid amount
        Double previousTotal = account.getTotalPaidAmount();

        if (previousTotal == null) {
            previousTotal = 0.0;
        }

        account.setTotalPaidAmount(
                roundToTwoDecimals(previousTotal + paymentAmount));

        // 10. Mark EMI as paid
        schedule.setPaymentStatus("PAID");
        schedule.setPaidDate(LocalDateTime.now());

        emiScheduleRepository.save(schedule);

        // 11. Check whether loan can be closed
        List<EmiSchedule> pendingEmis = emiScheduleRepository
                .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                        account.getLoanAccountId(), "PENDING");

        if (pendingEmis.isEmpty() && remaining <= 0.01) {
            account.setLoanStatus("CLOSED");
        }

        loanAccountRepository.save(account);

        // 12. Create EMI payment record
        LoanPayment payment = new LoanPayment();

        payment.setLoanAccount(account);
        payment.setEmiSchedule(schedule);
        payment.setPaymentAmount(paymentAmount);

        payment.setPaymentName(
                request.getPaymentName() != null
                        ? request.getPaymentName()
                        : "EMI_PAYMENT");

        payment.setPaymentMode(
                request.getPaymentMode() != null
                        ? request.getPaymentMode()
                        : "ONLINE");

        payment.setPaymentStatus("SUCCESS");

        LoanPayment savedPayment = loanPaymentRepository.save(payment);

        // 13. Return response
        return LoanPaymentResponse.builder()
                .paymentId(savedPayment.getPaymentId())
                .loanAccountId(account.getLoanAccountId())
                .emiScheduleId(schedule.getEmiScheduleId())
                .paymentAmount(roundToTwoDecimals(
                        savedPayment.getPaymentAmount()))
                .paymentDate(savedPayment.getPaymentDate())
                .paymentStatus(savedPayment.getPaymentStatus())
                .paymentName(savedPayment.getPaymentName())
                .paymentMode(savedPayment.getPaymentMode())
                .build();
    }

    // Common method for partial closure and foreclosure payment records
    @Override
    public LoanPayment recordNonEmiPayment(
            LoanAccount account,
            Double amount,
            String paymentName,
            String paymentMode) {

        if (account == null || amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "Valid loan account and payment amount are required");
        }

        LoanPayment payment = new LoanPayment();

        payment.setLoanAccount(account);
        payment.setEmiSchedule(null);
        payment.setPaymentAmount(roundToTwoDecimals(amount));

        payment.setPaymentName(
                paymentName != null ? paymentName : "LOAN_CLOSURE_PAYMENT");

        payment.setPaymentMode(
                paymentMode != null ? paymentMode : "ONLINE");

        payment.setPaymentStatus("SUCCESS");

        return loanPaymentRepository.save(payment);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
