package com.ampta.service;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.response.EligibilityResultResponse;

import java.util.List;

public interface EligibilityService {
    EligibilityResultResponse checkEligibility(EligibilityCheckRequest request);
    EligibilityResultResponse getEligibilityById(Long eligibilityId);
    EligibilityResultResponse getCustomerEligibility(Long customerId);
    List<EligibilityResultResponse> getAllEligibility();
}
