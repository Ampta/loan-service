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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanPaymentServiceImpl implements LoanPaymentService {

    private final LoanPaymentRepository loanPaymentRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final ModelMapper modelMapper;

    @Override
    public LoanPaymentResponse makePayment(
            LoanPaymentRequest request) throws BadRequestException {

        // 1. Fetch existing loan account
        LoanAccount account = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan account not found with id: "
                                        + request.getLoanAccountId()));

        // 2. Fetch EMI schedule
        EmiSchedule schedule = emiScheduleRepository
                .findById(request.getEmiScheduleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "EMI schedule not found"));


        // 3. Validate EMI belongs to this loan account
        if (!schedule.getLoanAccount().getLoanAccountId()
                .equals(account.getLoanAccountId())) {
            throw new BadRequestException(
                    "EMI does not belong to this loan account");
        }

        // 4. Validate payment amount
        Double paymentAmount = request.getPaymentAmount() == null
                ? null
                : request.getPaymentAmount().doubleValue();

        Double emiAmount = schedule.getEmiAmount();

        if (paymentAmount == null
                || emiAmount == null
                || paymentAmount <= 0
                || BigDecimal.valueOf(paymentAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .compareTo(
                        BigDecimal.valueOf(emiAmount)
                                .setScale(2, RoundingMode.HALF_UP)) != 0) {

            throw new BadRequestException(
                    "Invalid EMI payment amount");
        }

        // 5. Prevent duplicate payments
        if (!"PENDING".equalsIgnoreCase(
                schedule.getPaymentStatus())) {
            throw new BadRequestException(
                    "This EMI is not pending payment");
        }

        // 6. Mark EMI as paid
        schedule.setPaymentStatus("PAID");
        schedule.setPaidDate(LocalDateTime.now());

        // 7. Reduce outstanding principal
        Double principalAmount = schedule.getPrincipalAmount();
        Double outstanding = account.getOutstandingPrincipal();

        if (principalAmount != null && outstanding != null) {

            Double remaining = outstanding - principalAmount;

            remaining = Math.max(remaining, 0.0);

            account.setOutstandingPrincipal(
                    roundToTwoDecimals(remaining));
        }

        // 8. Update total paid amount
        Double previousTotal = account.getTotalPaidAmount();

        if (previousTotal == null) {
            previousTotal = 0.0;
        }

        account.setTotalPaidAmount(
                roundToTwoDecimals(previousTotal + paymentAmount));

        // 9. Save updated EMI
        emiScheduleRepository.save(schedule);

        // 10. Check remaining pending EMIs
        List<EmiSchedule> pending =
                emiScheduleRepository
                        .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                account.getLoanAccountId(),
                                "PENDING");

        if (pending.isEmpty()) {
            account.setLoanStatus("CLOSED");
        }

        // 11. Save existing loan account
        loanAccountRepository.save(account);

        // 12. Create payment record
        LoanPayment payment = new LoanPayment();

        payment.setLoanAccount(account);
        payment.setEmiSchedule(schedule);
        payment.setPaymentAmount(
                BigDecimal.valueOf(paymentAmount)
                        .setScale(2, RoundingMode.HALF_UP));

        payment.setPaymentName(
                request.getPaymentName() != null
                        ? request.getPaymentName()
                        : "EMI_PAYMENT");

        payment.setPaymentMode(
                request.getPaymentMode() != null
                        ? request.getPaymentMode()
                        : "ONLINE");

        payment.setPaymentStatus("SUCCESS");

        LoanPayment saved = loanPaymentRepository.save(payment);

        return modelMapper.map(
                saved, LoanPaymentResponse.class);
    }

    // Round Double monetary values to 2 decimal places
    private Double roundToTwoDecimals(Double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}