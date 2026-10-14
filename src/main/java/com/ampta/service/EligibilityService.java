package com.ampta.service;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.request.EligibilityDecisionRequest;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.dto.response.OfficerEligibilityCustomerResponse;

import java.util.List;

public interface EligibilityService {

    EligibilityResultResponse checkEligibility(
            EligibilityCheckRequest request
    );

    EligibilityResultResponse checkEligibilityForOfficer(
            EligibilityCheckRequest request,
            Long officerId
    );

    List<OfficerEligibilityCustomerResponse> getManualReviewCustomers(
            Long officerId
    );

    EligibilityResultResponse decideEligibility(
            Long eligibilityId,
            Long officerId,
            EligibilityDecisionRequest request
    );

    EligibilityResultResponse getEligibilityById(Long eligibilityId);

    EligibilityResultResponse getCustomerEligibility(Long customerId);

    List<EligibilityResultResponse> getAllEligibility();
}