package com.ampta.service;

import com.ampta.dto.request.ScoreCardRequest;
import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.entity.ScoreCard;

import java.util.List;

public interface ScoreCardService {

    ScoreCardResponse createOrGenerateScoreCard(ScoreCardRequest request);
    ScoreCardResponse getScoreCardById(Integer scoreCardId);
    ScoreCardResponse getCustomerScoreCard(Integer customerId);
    List<ScoreCardResponse> getAllScoreCards();
    ScoreCardResponse updateScoreCard(Integer scoreCardId, ScoreCardRequest request);

}
