package com.ampta.service.impl;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.entity.CibilReport;
import com.ampta.entity.Customer;
import com.ampta.entity.EligibilityResult;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.EligibilityResultRepository;
import com.ampta.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityResultRepository eligibilityResultRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final ModelMapper modelMapper;


    @Override
    public EligibilityResultResponse checkEligibility(EligibilityCheckRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        CibilReport latestCibil = cibilReportRepository.findFirstByCustomer_CustomerIdOrderByCheckDateDesc(customer.getCustomerId()).orElse(null);
        int cibilScore = latestCibil != null && latestCibil.getCibilScore() != null ? latestCibil.getCibilScore() : 750;

        boolean isEligible = true;
        String rejectionReason = null;
        Double loanAmount = 0.0;

        // Rule 1: CIBIL Score Rejection Check (< 600 is rejected)
        if (cibilScore < 600) {
            isEligible = false;
            rejectionReason = "CIBIL score is below the minimum threshold of 600 (Current: " + cibilScore + ")";
        } else {
            // Rule 2: Loan Amount Allocation based purely on CIBIL Score Tiers
            if (cibilScore >= 600 && cibilScore < 670) {
                loanAmount = 100000.00; // Tier 1: 1 Lakh
            } else if (cibilScore >= 670 && cibilScore < 750) {
                loanAmount = 300000.00; // Tier 2: 3 Lakhs
            } else if (cibilScore >= 750 && cibilScore <= 900) {
                loanAmount = 500000.00; // Tier 3: 5 Lakhs
            } else {
                loanAmount = 200000.00; // Default fallback
            }
        }

        // Populate entity fields matching your exact structure
        EligibilityResult result = new EligibilityResult();
        result.setCustomer(customer);
        result.setCibilScore(cibilScore);
        result.setIsEligible(isEligible);
        result.setLoanAmount(loanAmount);
        result.setRejectionReason(rejectionReason);
        EligibilityResult saved = eligibilityResultRepository.save(result);

        return modelMapper.map(saved, EligibilityResultResponse.class);
    }

    @Override
    public EligibilityResultResponse getEligibilityById(Long eligibilityId) {
        return null;
    }

    @Override
    public EligibilityResultResponse getCustomerEligibility(Long customerId) {
        return null;
    }

    @Override
    public List<EligibilityResultResponse> getAllEligibility() {
        return List.of();
    }
}
