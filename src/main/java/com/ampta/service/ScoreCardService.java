package com.ampta.service;

import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.dto.response.ScoreCardResponse;

import java.util.List;

public interface ScoreCardService {

//    List<OfficerEligibilityCustomerResponse>
//    getCustomersForScoreCards();
//
//    ScoreCardResponse getOrGenerateScoreCard(Long customerId);
// Get customers and their eligibility status for officer's list
List<OfficerEligibilityCustomerResponse> getCustomersForScoreCards();

    // Generate a scorecard only when eligibility is approved,
    // or refresh an existing scorecard
    ScoreCardResponse getOrGenerateScoreCard(Long customerId);
}