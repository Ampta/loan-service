
        package com.ampta.service.impl;

import com.ampta.config.LoanMapper;
import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.response.LoanApplicationResponse;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.entity.enums.LoanType;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.LoanDealRepository;
import com.ampta.service.LoanService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanDealRepository loanDealRepository;
    private final EmiCalculationServiceImpl emiCalculationService;
    private final LoanMapper loanMapper;

    private double getInterestRate(LoanType loanType) {

        if (loanType == LoanType.HOME) {
            return 15.0;
        }

        if (loanType == LoanType.VEHICLE) {
            return 10.0;
        }

        throw new IllegalArgumentException(
                "This loan type is not available: " + loanType);
    }

    @Override
    @Transactional
    public LoanApplicationResponse applyLoan(
            Long userId,
            LoanApplicationRequest request) {

        LoanDeal loanDeal = loanMapper.toEntity(request);

        // Set the logged-in customer's ID
        loanDeal.setUserId(userId);

        // Set interest rate
        double interestRate = getInterestRate(request.getLoanType());
        loanDeal.setInterestRate(interestRate);

        // Calculate EMI
        double emi = emiCalculationService.calculateEmi(
                request.getLoanAmount(),
                interestRate,
                request.getTenureMonths()
        );

        loanDeal.setEmiAmount(emi);

        // Amount is assigned after approval
        loanDeal.setApprovedAmount(request.getLoanAmount());

        // Initial loan status
        loanDeal.setStatus(LoanStatus.SUBMITTED);

        // Save loan application
        LoanDeal savedDeal = loanDealRepository.save(loanDeal);

        return loanMapper.toResponse(savedDeal);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanById(Long dealId) {

        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan not found with ID: " + dealId));

        return loanMapper.toResponse(loanDeal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllLoans() {

        return loanDealRepository.findAll()
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteLoan(Long dealId) {

        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan not found with ID: " + dealId));

        loanDealRepository.delete(loanDeal);
    }
}
