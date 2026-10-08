package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.entity.ScoreCard;
import com.ampta.service.ScoreCardService;
import com.ampta.utils.Endpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Endpoints.V1_SCORE_CARD)
@RequiredArgsConstructor
public class ScoreCardController {

    private final ScoreCardService scoreCardService;

    public ResponseEntity<ApiResponse<?>> createScoreCard(@RequestBody ScoreCard request){
        return null;
    }

}
