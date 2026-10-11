package com.ampta.service.impl;

import com.ampta.LoanServiceApplication;
import com.ampta.config.LoanMapper;
import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.request.LoanApplicationResponse;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.LoanStatus;
import com.ampta.entity.LoanType;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.LoanDealRepository;
import com.ampta.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanDealRepository loanDealRepository;

    private final EmiCalculationServiceImpl emiCalculationService;

    private final LoanMapper loanMapper;

    private double getInterestRate(LoanType loanType)
    {
        if(loanType == LoanType.HOME)
        {
            return 15.0;
        }

        if (loanType == LoanType.VEHICLE)
        {
            return 10.0;
        }

        throw new IllegalArgumentException("This Loan Type Not Available "+ loanType);
    }

    @Override
    public LoanApplicationResponse applyLoan(Long userId, LoanApplicationRequest request) {

        LoanDeal loanDeal=loanMapper.toEntity(request);

        loanDeal.setUserId(userId);

        double interestRate= getInterestRate(request.getLoanType());

        loanDeal.setInterestRate(interestRate);

        //calculate Emi
        double emi=emiCalculationService.calculateEmi(request.getLoanAmount(),
                interestRate,request.getTenureMonths());

        loanDeal.setEmiAmount(emi);

        loanDeal.setApprovedAmount(null);

        loanDeal.setStatus(LoanStatus.SUBMITTED);

        LoanDeal saveDeal=loanDealRepository.save(loanDeal);

        return loanMapper.toResponse(saveDeal);
    }

    @Override
    public LoanApplicationResponse getLoanById(Long dealId) {

        LoanDeal loanDeal =
                loanDealRepository.findById(dealId)
                        .orElseThrow(()-> new LoanNotFoundException("Loan not found with id :"+ dealId));

        return loanMapper.toResponse(loanDeal);
    }

    @Override
    public List<LoanApplicationResponse> getAllLoans() {

        List<LoanDeal> serviceApplication=loanDealRepository.findAll();

        return serviceApplication.stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteLoan(Long dealId) {

        LoanDeal loanDeal=loanDealRepository.findById(dealId)
                .orElseThrow(()-> new LoanNotFoundException("Loan Not found :"+ dealId));

        loanDealRepository.delete(loanDeal);

    }


}
