package com.ampta.controller;

import com.ampta.dto.request.ScoreCardRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.entity.ScoreCard;
import com.ampta.service.ScoreCardService;
import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Endpoints.V1_SCORE_CARD)
@RequiredArgsConstructor
@Slf4j
public class ScoreCardController {

    private final ScoreCardService scoreCardService;

    public ResponseEntity<ApiResponse<?>> createScoreCard(@RequestBody ScoreCard request){
        return null;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScoreCardResponse>> createScoreCard(@Valid @RequestBody ScoreCardRequest request) {
        log.info("REST request to generate/create score card for customer: {}", request.getCustomerId());
        ScoreCardResponse response = scoreCardService.createOrGenerateScoreCard(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Score card created successfully", response), HttpStatus.CREATED);
    }
}
