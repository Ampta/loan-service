
package com.ampta.service.impl;

import com.ampta.dto.request.EmiPaymentStatusRequest;
import com.ampta.dto.request.EmiScheduleRequest;
import com.ampta.dto.response.EmiScheduleResponse;
import com.ampta.entity.EmiSchedule;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanDeal;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.EmiScheduleRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.service.EmiScheduleService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ModelMapper modelMapper;

    // Create EMI schedule
    @Override
    @Transactional
    public List<EmiScheduleResponse> createEmiSchedule(EmiScheduleRequest request) {
        LoanAccount loanAccount = loanAccountRepository
                .findById(request.getLoanAccountId()).orElseThrow(()
                        -> new RuntimeException("Loan account not found"));

        // Fetch associated loan deal
        LoanDeal loanDeal = loanAccount.getLoanDeal();

        if (loanDeal == null) {
            throw new RuntimeException(
                    "Loan deal not found for loan account");
        }

        // Get loan details
        double openingBalance = loanAccount.getLoanAmount();
        double interestRate = loanAccount.getInterestRate();
        int tenureMonths = loanAccount.getTenureMonths();
        double emiAmount = loanAccount.getEmiAmount();

        // Validate EMI details
        if (openingBalance <= 0 || tenureMonths <= 0
                || interestRate < 0 || emiAmount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid loan amount, tenure, interest rate, or EMI");
        }

        if (loanAccount.getDisbursementDate() == null) {
            throw new IllegalArgumentException(
                    "Disbursement date is required");
        }

        LocalDate disbursementDate =
                loanAccount.getDisbursementDate().toLocalDate();

        int emiDay = loanDeal.getEmiDay();

        if (emiDay < 1 || emiDay > 31) {
            throw new IllegalArgumentException(
                    "EMI day must be between 1 and 31");
        }


        List<EmiScheduleResponse> responseList = new ArrayList<>();
        for (int i = 1; i <= tenureMonths; i++) {
            LocalDate targetMonth = disbursementDate.plusMonths(i);
            LocalDate dueDate = targetMonth.withDayOfMonth(
                    Math.min(emiDay, targetMonth.lengthOfMonth())
            );

            // Calculate monthly interest
            double interestAmount = roundToTwoDecimals(
                    openingBalance * interestRate / 12 / 100
            );

            // Calculate principal component
            double principalAmount = roundToTwoDecimals(
                    emiAmount - interestAmount
            );

            // Do not allow negative closing balance
            double closingBalance = roundToTwoDecimals(
                    Math.max(0, openingBalance - principalAmount)
            );



            // Create EMI schedule entity
            EmiSchedule schedule = new EmiSchedule();

            schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNo((long) i);
            schedule.setDisbursementDate(disbursementDate);
            schedule.setDueDate(dueDate);
            schedule.setInterestRate(interestRate);
            schedule.setOpeningBalance(openingBalance);
            schedule.setInterestAmount(interestAmount);
            schedule.setPrincipalAmount(principalAmount);
            schedule.setClosingBalance(closingBalance);
            schedule.setEmiAmount(emiAmount);
            schedule.setPaymentStatus("PENDING");

            // Save schedule
            EmiSchedule saved = emiScheduleRepository.save(schedule);

            // Convert entity to response DTO
            responseList.add(modelMapper.map(saved, EmiScheduleResponse.class));
            // Next month's opening balance
            openingBalance = closingBalance;
        }

        return responseList;
    }



    // Get paginated EMI schedules for a loan account
    @Override
    @Transactional(readOnly = true)
    public Page<EmiScheduleResponse> getEmiSchedulesByLoanAccountId(Integer loanAccountId, Pageable pageable) {
        Page<EmiSchedule> schedules = emiScheduleRepository.findByLoanAccount_LoanAccountId(loanAccountId, pageable);
        return schedules.map(schedule -> modelMapper.map(schedule, EmiScheduleResponse.class));
    }

    //get Emi
    @Override
    public EmiScheduleResponse getEmiScheduleById(Integer emiScheduleId) {
        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found with id: " + emiScheduleId));
        return modelMapper.map(schedule, EmiScheduleResponse.class);
    }


    @Override
    public EmiScheduleResponse updatePaymentStatus(Integer emiScheduleId, EmiPaymentStatusRequest request) {
        EmiSchedule schedule = emiScheduleRepository.findById(emiScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI Schedule not found with id: " + emiScheduleId));
        schedule.setPaymentStatus(request.getPaymentStatus());
        if ("PAID".equalsIgnoreCase(request.getPaymentStatus())) {
            schedule.setPaidDate(LocalDateTime.now());
        }
        if (request.getCancellationReason() != null) {
            schedule.setCancellationReason(request.getCancellationReason());
        }
        EmiSchedule saved = emiScheduleRepository.save(schedule);
        return modelMapper.map(saved, EmiScheduleResponse.class);
    }

    // Round amount to two decimal places
    private double roundToTwoDecimals(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
