
package com.ampta.service.impl;

import com.ampta.config.LoanMapper;
import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.response.LoanApplicationResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.entity.enums.LoanType;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.LoanDealRepository;
import com.ampta.repository.ScoreCardRepository;
import com.ampta.service.LoanService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanDealRepository loanDealRepository;

    private final CustomerRepository customerRepository;

    private final EmiCalculationServiceImpl emiCalculationService;

    private final LoanMapper loanMapper;

    private final ScoreCardRepository scoreCardRepository;

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
            Long customerId,
            LoanApplicationRequest request) {

        // 1. Find customer using CustomerRepository
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found with ID: " + customerId));

        // 2. Convert request DTO to LoanDeal entity
        LoanDeal loanDeal = loanMapper.toEntity(request);

        // 3. Set Customer instead of User ID
        loanDeal.setCustomer(customer);

        // 4. Set interest rate
        double interestRate = getInterestRate(request.getLoanType());
        loanDeal.setInterestRate(interestRate);

        // 5. Calculate EMI
        double emi = emiCalculationService.calculateEmi(
                request.getLoanAmount(),
                interestRate,
                request.getTenureMonths());

        loanDeal.setEmiAmount(emi);

        // 6. Set approved amount
        loanDeal.setApprovedAmount(request.getLoanAmount());

        // 7. Set initial status
        loanDeal.setStatus(LoanStatus.SUBMITTED);

        // 8. Save loan application
        LoanDeal savedDeal = loanDealRepository.save(loanDeal);

        // 9. Return response
        return loanMapper.toResponse(savedDeal);
    }


//    @Override
//    @Transactional
//    public LoanApplicationResponse applyLoan(Long customerId,LoanApplicationRequest request)
//    {
//        Customer customer=customerRepository.findById(customerId)
//                .orElseThrow(()-> new RuntimeException("Customer not found with id :"+ customerId));
//
//        LoanDeal loanDeal=loanMapper.toEntity(request);
//
//        loanDeal.setCustomer(customer);
//
//        double interestRate=getInterestRate(request.getLoanType());
//        loanDeal.setInterestRate(interestRate);
//
//        //calculate emi
//        double emi=emiCalculationService.calculateEmi(loanDeal.getLoanAmount(),
//                interestRate,loanDeal.getTenureMonths());
//
//        loanDeal.setEmiAmount(emi);
//        loanDeal.setEmiDay(request.getEmiDay());
//
//        loanDeal.setLoanAmount(request.getLoanAmount());
//
//        ScoreCard  scoreCard=scoreCardRepository.findByCustomerId(customerId)
//                .orElseThrow(()-> new RuntimeException("Customer Not Found In ScoreCart Table :"+ customerId));
//
//        Double approvedAmount=scoreCard.getEligibleLoanAmount();
//
//        if(approvedAmount == null)
//        {
//            throw new IllegalStateException("Approved Amount is not Available for this user");
//        }
//
//        if(loanDeal.getLoanAmount() >= approvedAmount)
//        {
//            throw new IllegalArgumentException("Loan Amount is greater than approved amount");
//        }
//
//
//        loanDeal.setStatus(LoanStatus.SUBMITTED);
//
//        LoanDeal saveLoanDeal=loanDealRepository.save(loanDeal);
//
//        return loanMapper.toResponse(saveLoanDeal);
//
//    }


    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanById(Long dealId) {

        LoanDeal loanDeal = loanDealRepository.findById(dealId).orElseThrow(() ->
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

        LoanDeal loanDeal=loanDealRepository.findById(dealId)
                .orElseThrow(() -> new LoanNotFoundException("Loan Not Found of this Id :"+ dealId));

        loanDealRepository.delete(loanDeal);
    }
}
