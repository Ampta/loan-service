
package com.ampta.service.impl;

import com.ampta.dto.request.EmiPaymentStatusRequest;
import com.ampta.dto.request.EmiScheduleRequest;
import com.ampta.dto.response.EmiPaymentSummaryResponse;
import com.ampta.dto.response.EmiScheduleResponse;
import com.ampta.entity.EmiSchedule;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.PenaltyCharge;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.EmiScheduleRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.PenaltyChargeRepository;
import com.ampta.service.EmiScheduleService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final ModelMapper modelMapper;

    // Create EMI schedule
    @Override
    public List<EmiScheduleResponse> createEmiSchedule(
            EmiScheduleRequest request) {

        if (request == null || request.getLoanAccountId() == null) {
            throw new IllegalArgumentException(
                    "Loan account ID is required");
        }

        // Prevent duplicate EMI schedule generation
        if (emiScheduleRepository
                .existsByLoanAccount_LoanAccountId(
                        request.getLoanAccountId())) {

            throw new IllegalArgumentException(
                    "EMI schedule already exists for loan account ID: "
                            + request.getLoanAccountId()
            );
        }


        // 1. Fetch loan account
        LoanAccount loanAccount = loanAccountRepository
                .findById(request.getLoanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found with id: "
                                + request.getLoanAccountId()));

        // 2. Fetch associated loan deal
        LoanDeal loanDeal = loanAccount.getLoanDeal();

        if (loanDeal == null) {
            throw new IllegalArgumentException(
                    "Loan deal not found for this loan account");
        }

        // 3. Validate required loan details
        Double loanAmount = loanAccount.getLoanAmount();
        Double interestRateValue = loanAccount.getInterestRate();
        Integer tenureMonthsValue = loanAccount.getTenureMonths();
        Double emiAmountValue = loanAccount.getEmiAmount();
        Integer emiDayValue = loanDeal.getEmiDay();

        if (loanAmount == null
                || interestRateValue == null
                || tenureMonthsValue == null
                || emiAmountValue == null
                || emiDayValue == null) {
            throw new IllegalArgumentException(
                    "Loan amount, interest rate, tenure, EMI amount "
                            + "and EMI day are required");
        }

        double openingBalance = roundToTwoDecimals(loanAmount);
        double interestRate = interestRateValue;
        int tenureMonths = tenureMonthsValue;
        double emiAmount = roundToTwoDecimals(emiAmountValue);
        int emiDay = emiDayValue;

        if (openingBalance <= 0
                || tenureMonths <= 0
                || interestRate < 0
                || emiAmount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid loan amount, tenure, interest rate or EMI amount");
        }

        if (emiDay < 1 || emiDay > 31) {
            throw new IllegalArgumentException(
                    "EMI day must be between 1 and 31");
        }

        // 4. Validate disbursement date
        if (loanAccount.getDisbursementDate() == null) {
            throw new IllegalArgumentException(
                    "Disbursement date is required");
        }

        LocalDate disbursementDate =
                loanAccount.getDisbursementDate().toLocalDate();

        // 5. Prepare response list
        List<EmiScheduleResponse> responseList = new ArrayList<>();

        // 6. Generate monthly EMI schedule
        for (int i = 1; i <= tenureMonths; i++) {

            if (openingBalance <= 0) {
                break;
            }

            // Calculate due date
            LocalDate targetMonth = disbursementDate.plusMonths(i);

            LocalDate dueDate = targetMonth.withDayOfMonth(
                    Math.min(emiDay, targetMonth.lengthOfMonth()));

            // Calculate monthly interest
            double interestAmount = roundToTwoDecimals(
                    openingBalance * interestRate / 12.0 / 100.0);

            // EMI must cover at least the monthly interest
            if (emiAmount <= interestAmount) {
                throw new IllegalArgumentException(
                        "EMI amount must be greater than monthly interest");
            }

            // Do not reduce more principal than remains
            double principalAmount = roundToTwoDecimals(
                    Math.min(openingBalance, emiAmount - interestAmount));

            // Final installment can be smaller than regular EMI
            double actualEmiAmount = roundToTwoDecimals(
                    principalAmount + interestAmount);

            // Calculate closing balance
            double closingBalance = roundToTwoDecimals(
                    Math.max(0.0, openingBalance - principalAmount));

            // 7. Create EMI schedule entity
            EmiSchedule schedule = new EmiSchedule();

            schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNo((long) i);
            schedule.setDisbursementDate(disbursementDate);
            schedule.setDueDate(dueDate);
            schedule.setInterestRate(interestRate);
            schedule.setOpeningBalance(
                    roundToTwoDecimals(openingBalance));
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmiAmount(actualEmiAmount);
            schedule.setPaymentStatus("PENDING");

            // 8. Save EMI schedule
            EmiSchedule savedSchedule =
                    emiScheduleRepository.save(schedule);

            // 9. Convert entity to response DTO
            responseList.add(
                    modelMapper.map(
                            savedSchedule,
                            EmiScheduleResponse.class));

            // 10. Carry closing balance to next month
            openingBalance = closingBalance;
        }

        return responseList;
    }



    // Get paginated EMI schedules for a loan account
    @Override
    @Transactional(readOnly = true)
    public Page<EmiScheduleResponse> getEmiSchedulesByLoanAccountId(
            Long loanAccountId,
            Pageable pageable) {

        Page<EmiSchedule> schedules = emiScheduleRepository.findByLoanAccount_LoanAccountId(loanAccountId, pageable);
        return schedules.map(
                schedule -> modelMapper.map(
                        schedule, EmiScheduleResponse.class));
    }


    // Get EMI by ID
    @Override
    @Transactional(readOnly = true)
    public EmiScheduleResponse getEmiScheduleById(Long emiScheduleId) {
        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                 .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI schedule not found with id: "
                                + emiScheduleId));
        return modelMapper.map(
                schedule, EmiScheduleResponse.class);
    }


    // Update EMI payment status
    @Override
    @Transactional
    public EmiScheduleResponse updatePaymentStatus(
            Long loanAccountId,
            Long emiScheduleId,
            EmiPaymentStatusRequest request) {

        // 1. Validate request
        if (request == null
                || request.getPaymentStatus() == null
                || request.getPaymentStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "Payment status is required");
        }

        // 2. Check whether loan account exists
        loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found with id: " + loanAccountId));

        // 3. Fetch EMI schedule
        EmiSchedule schedule = emiScheduleRepository
                .findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI schedule not found with id: " + emiScheduleId));

        // 4. Validate EMI belongs to this loan account
        if (schedule.getLoanAccount() == null
                || !schedule.getLoanAccount()
                .getLoanAccountId()
                .equals(loanAccountId)) {

            throw new IllegalArgumentException(
                    "EMI schedule does not belong to loan account: "
                            + loanAccountId);
        }

        // 5. Normalize status
        String status = request.getPaymentStatus()
                .trim()
                .toUpperCase(java.util.Locale.ROOT);

        // 6. Validate allowed statuses
        if (!List.of("PENDING", "CANCELLED", "BOUNCED")
                .contains(status)) {

            throw new IllegalArgumentException(
                    "Invalid EMI payment status: " + status
                            + ". PAID status must be set through the verified payment flow.");
        }

        String currentStatus = schedule.getPaymentStatus();

        // 7. Never change an already paid EMI
        if ("PAID".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException(
                    "A paid EMI cannot be changed to another status");
        }

        // 8. Update status
        schedule.setPaymentStatus(status);

        // 9. Set cancellation reason only for cancellation
        if ("CANCELLED".equals(status)) {
            schedule.setCancellationReason(
                    request.getCancellationReason());
        }

        // 10. Save changes
        EmiSchedule savedSchedule =
                emiScheduleRepository.save(schedule);

        // 11. Convert entity to response DTO
        EmiScheduleResponse response =
                modelMapper.map(
                        savedSchedule,
                        EmiScheduleResponse.class);

        // 12. Explicitly set loan account ID
        response.setLoanAccountId(loanAccountId);

        return response;
    }



    // Get all EMI schedule details by Loan Account ID
    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getEmiScheduleDetailsByLoanAccountId(Long loanAccountId) {
        // 1. Validate loan account
        loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan account not found with id: " + loanAccountId));

        // 2. Fetch all EMI schedules for this loan account
        List<EmiSchedule> schedules =
                emiScheduleRepository
                        .getEmiScheduleDetailsByLoanAccountId(loanAccountId);

        // 3. Convert entities to response DTOs
        return schedules.stream()
                .map(schedule -> {

                    EmiScheduleResponse response =
                            modelMapper.map(
                                    schedule,
                                    EmiScheduleResponse.class);

                    // Explicitly set loan account ID
                    response.setLoanAccountId(loanAccountId);

                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
    }





    @Override
    public EmiPaymentSummaryResponse getPaymentSummary(Long loanAccountId, Long emiScheduleId) {

        LoanAccount loanAccount = loanAccountRepository
                .findById(loanAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan account not found: " + loanAccountId));

        EmiSchedule emiSchedule = emiScheduleRepository
                .findById(emiScheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "EMI schedule not found: " + emiScheduleId));

        // Check whether this EMI belongs to the requested loan account
        if (emiSchedule.getLoanAccount() == null
                || !emiSchedule.getLoanAccount()
                .getLoanAccountId()
                .equals(loanAccount.getLoanAccountId())) {

            throw new IllegalArgumentException(
                    "EMI schedule does not belong to this loan account");
        }

        // Get EMI amount
        Double emiAmount = emiSchedule.getEmiAmount();

        // Default penalty details
        Double bounceCharge = 0.0;
        Double dailyPenalty = 0.0;
        Integer overdueDays = 0;
        Double penaltyAmount = 0.0;
        String penaltyStatus = "NO_PENALTY";

        // Find penalty using EMI schedule ID
        Optional<PenaltyCharge> penaltyOptional =
                penaltyChargeRepository
                        .findByEmiSchedule_EmiScheduleId(emiScheduleId);

        if (penaltyOptional.isPresent()) {
            PenaltyCharge penalty = penaltyOptional.get();
            // Get penalty details
            bounceCharge = penalty.getBounceCharge() == null
                    ? 0.0 : penalty.getBounceCharge();
            dailyPenalty = penalty.getDailyPenalty() == null
                    ? 0.0 : penalty.getDailyPenalty();
            overdueDays = penalty.getOverdueDays() == null
                    ? 0 : penalty.getOverdueDays();
            penaltyStatus = penalty.getStatus() == null
                    ? "UNPAID" : penalty.getStatus();

            // Add penalty only when it is unpaid
            if ("PENDING".equalsIgnoreCase(penaltyStatus)) {
                penaltyAmount = penalty.getPenaltyAmount() == null
                        ? 0.0 : penalty.getPenaltyAmount();
            }
        }

        // Calculate total payable amount
        Double totalPayable = emiAmount + penaltyAmount;

        // Map EMI entity to response DTO
        EmiPaymentSummaryResponse response =
                modelMapper.map(
                        emiSchedule,
                        EmiPaymentSummaryResponse.class);

        // Set response fields
        response.setLoanAccountId(loanAccount.getLoanAccountId());
        response.setEmiScheduleId(emiSchedule.getEmiScheduleId());
        response.setEmiAmount(emiAmount);

        response.setBounceCharge(bounceCharge);
        response.setDailyPenalty(dailyPenalty);
        response.setOverdueDays(overdueDays);

        response.setPenaltyAmount(penaltyAmount);
        response.setTotalPayable(totalPayable);
        response.setEmiStatus(emiSchedule.getPaymentStatus());
        response.setPenaltyStatus(penaltyStatus);

        return response;
    }


    private double roundToTwoDecimals(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
