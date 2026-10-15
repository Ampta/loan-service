package com.ampta.service;

import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.dto.response.ScoreCardResponse;

import java.util.List;

public interface ScoreCardService {

    List<OfficerEligibilityCustomerResponse>
    getCustomersForScoreCards();

    ScoreCardResponse getOrGenerateScoreCard(Long customerId);
}