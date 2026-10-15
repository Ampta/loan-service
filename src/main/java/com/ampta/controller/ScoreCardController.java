package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.service.ScoreCardService;

import com.ampta.utils.Endpoints;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(Endpoints.V1_SCORE_CARD)
public class ScoreCardController {

    private final ScoreCardService scoreCardService;


    // Generate or refresh a customer's scorecard.
    @PostMapping("/customers/{customerId}/generate")
    public ResponseEntity<ApiResponse<ScoreCardResponse>>
    generateScoreCard(
            @PathVariable Long customerId,
            @RequestParam Long officerId) {

        ScoreCardResponse response =
                scoreCardService.generateScoreCard(
                        customerId,
                        officerId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Scorecard generated successfully",
                        response
                )
        );
    }


    // View the latest saved scorecard.
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<ScoreCardResponse>>
    getLatestScoreCard(
            @PathVariable Long customerId,
            @RequestParam Long officerId) {

        ScoreCardResponse response =
                scoreCardService.getLatestScoreCard(
                        customerId,
                        officerId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Scorecard fetched successfully",
                        response
                )
        );
    }
}