package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.service.ScoreCardService;
import com.ampta.utils.Endpoints;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(Endpoints.V1_SCORE_CARD)
//@PreAuthorize("hasRole('OFFICER')")
public class ScoreCardController {

    private final ScoreCardService scoreCardService;

    // 1. Officer fetches the customer list.
    @GetMapping("/customers")
    public ResponseEntity<
            ApiResponse<List<OfficerEligibilityCustomerResponse>>>
    getCustomersForScoreCards() {

        List<OfficerEligibilityCustomerResponse> response =
                scoreCardService.getCustomersForScoreCards();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customers fetched successfully",
                        response
                )
        );
    }

    // 2. Officer selects a customer and views the scorecard.
    // Backend automatically generates it if required.
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<ScoreCardResponse>>
    getScoreCard(@PathVariable Long customerId) {

        ScoreCardResponse response =
                scoreCardService.getOrGenerateScoreCard(customerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Scorecard fetched successfully",
                        response
                )
        );
    }
}