
package com.ampta.service.impl;

import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.request.PaymentStatusRequest;
import com.ampta.dto.response.LoanPaymentResponse;
import com.ampta.entity.EmiSchedule;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanPayment;
import com.ampta.entity.PenaltyCharge;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.EmiScheduleRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.LoanPaymentRepository;
import com.ampta.repository.PenaltyChargeRepository;
import com.ampta.service.LoanPaymentService;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
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
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final ModelMapper modelMapper;

    @Override
    public LoanPaymentResponse makePayment(LoanPaymentRequest request)
            throws BadRequestException {

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

        // 4. Verify EMI belongs to this loan account
        if (schedule.getLoanAccount() == null
                || !schedule.getLoanAccount().getLoanAccountId()
                .equals(account.getLoanAccountId())) {

            throw new BadRequestException(
                    "EMI does not belong to this loan account");
        }

        // 5. EMI must be pending or bounced
        String emiStatus = schedule.getPaymentStatus();

        if (!"PENDING".equalsIgnoreCase(emiStatus)
                && !"BOUNCED".equalsIgnoreCase(emiStatus)) {

            throw new BadRequestException(
                    "This EMI is not pending payment");
        }

        // 6. Fetch original EMI amount
        Double emiValue = schedule.getEmiAmount();

        if (emiValue == null || emiValue <= 0) {
            throw new BadRequestException(
                    "EMI amount must be positive");
        }

        double emiAmount = roundToTwoDecimals(emiValue);


        // 7. Find penalty associated with this EMI
        PenaltyCharge penalty = penaltyChargeRepository
                .findByEmiSchedule_EmiScheduleId(
                        schedule.getEmiScheduleId())
                .orElse(null);

        double penaltyAmount = 0.0;

// Include penalty only when its status is PENDING
        if (penalty != null
                && "PENDING".equalsIgnoreCase(penalty.getStatus())) {

            Double penaltyValue = penalty.getPenaltyAmount();

            penaltyAmount = penaltyValue == null
                    ? 0.0
                    : roundToTwoDecimals(penaltyValue);

            if (penaltyAmount < 0) {
                throw new BadRequestException(
                        "Penalty amount cannot be negative");
            }
        }

        // 8. Calculate total without modifying original EMI
        double totalPayable = roundToTwoDecimals(
                emiAmount + penaltyAmount);

        // 9. Validate payment amount BEFORE changing database records
        double paymentAmount =
                roundToTwoDecimals(request.getPaymentAmount());

        if (paymentAmount <= 0) {
            throw new BadRequestException(
                    "Payment amount must be positive");
        }

        if (Math.abs(paymentAmount - totalPayable) > 0.001) {
            throw new BadRequestException(
                    "Incorrect payment amount. EMI amount: "
                            + emiAmount
                            + ", unpaid penalty: "
                            + penaltyAmount
                            + ", total payable: "
                            + totalPayable);
        }

        // 10. Validate principal and outstanding balance
        Double principalValue = schedule.getPrincipalAmount();
        Double outstandingValue = account.getOutstandingPrincipal();

        if (principalValue == null || outstandingValue == null) {
            throw new BadRequestException(
                    "Principal amount or outstanding balance is missing");
        }

        double principalAmount =
                roundToTwoDecimals(principalValue);

        double outstanding =
                roundToTwoDecimals(outstandingValue);

        if (principalAmount < 0 || outstanding < 0) {
            throw new BadRequestException(
                    "Principal amount and outstanding balance cannot be negative");
        }

        if (principalAmount > outstanding + 0.001) {
            throw new BadRequestException(
                    "EMI principal cannot exceed outstanding principal");
        }

        // 11. Create payment record
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

        /*
         * This follows your current implementation, which treats
         * the payment as successful immediately.
         *
         * For a real payment gateway, set SUCCESS only after
         * verifying successful payment confirmation.
         */
        payment.setPaymentStatus("SUCCESS");

        LoanPayment savedPayment =
                loanPaymentRepository.save(payment);

        // 12. Update EMI and penalty only for successful payment
        if ("SUCCESS".equalsIgnoreCase(
                savedPayment.getPaymentStatus())) {

            // Mark EMI as paid
            schedule.setPaymentStatus("PAID");
            schedule.setPaidDate(LocalDateTime.now());

            emiScheduleRepository.save(schedule);

            // Mark penalty as paid only if an unpaid penalty was included
            if (penalty != null && penaltyAmount > 0
                    && "PENDING".equalsIgnoreCase(penalty.getStatus())) {

                penalty.setStatus("PAID");
                penaltyChargeRepository.save(penalty);
            }

            // 13. Reduce principal by EMI principal only
            double remaining = roundToTwoDecimals(
                    Math.max(0.0, outstanding - principalAmount));

            account.setOutstandingPrincipal(remaining);

            // 14. Update total collected amount
            Double previousTotal = account.getTotalPaidAmount();

            if (previousTotal == null) {
                previousTotal = 0.0;
            }

            account.setTotalPaidAmount(
                    roundToTwoDecimals(
                            previousTotal + paymentAmount));

            // 15. Check loan closure
            List<EmiSchedule> pendingEmis =
                    emiScheduleRepository
                            .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                    account.getLoanAccountId(),
                                    "PENDING");

            List<EmiSchedule> bouncedEmis =
                    emiScheduleRepository
                            .findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
                                    account.getLoanAccountId(),
                                    "BOUNCED");

            if (pendingEmis.isEmpty()
                    && bouncedEmis.isEmpty()
                    && remaining <= 0.01) {

                account.setLoanStatus("CLOSED");
            }

            loanAccountRepository.save(account);
        }

        // 16. Build response
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


    @Override
    public LoanPaymentResponse getPaymentById(Long paymentId) {
        LoanPayment loanPayment = loanPaymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));

        return modelMapper.map(
                loanPayment, LoanPaymentResponse.class);
    }

    @Override
    public List<LoanPaymentResponse> getAllPayment() {
        List<LoanPayment> payments = loanPaymentRepository.findAll();
        return payments.stream()
                .map(payment -> modelMapper.map(
                        payment, LoanPaymentResponse.class))
                .toList();
    }

    @Override
    public LoanPaymentResponse updatePaymentStatus(
            Long paymentId, PaymentStatusRequest request) {

        LoanPayment loanPayment = loanPaymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));

        loanPayment.setPaymentStatus(request.getStatus());
        LoanPayment savedPayment =
                loanPaymentRepository.save(loanPayment);
        return modelMapper.map(
                savedPayment, LoanPaymentResponse.class);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
