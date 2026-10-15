package com.ampta.service;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.request.EligibilityDecisionRequest;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.dto.response.OfficerEligibilityCustomerResponse;

import java.util.List;

public interface EligibilityService {

    List<OfficerEligibilityCustomerResponse>
    getManualReviewCustomers();

    EligibilityResultResponse checkEligibility(Long customerId);

    EligibilityResultResponse decideEligibility(
            Long eligibilityId,
            EligibilityDecisionRequest request
    );
}