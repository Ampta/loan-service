package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.CibilScoreResponse;
import com.ampta.service.CibilService;
import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(Endpoints.V1_CIBIL)
@RequiredArgsConstructor
@Slf4j
public class CibilController {

    private final CibilService cibilService;

    // Calculate customer's CIBIL score
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CibilScoreResponse>> checkScore(
            @PathVariable Long customerId) {

        log.info(
                "CIBIL score calculation requested for customerId={}",
                customerId
        );

        CibilScoreResponse response =
                cibilService.calculateCibilScore(customerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "CIBIL score calculated successfully",
                        response
                )
        );
    }


    // Get customer's latest CIBIL score and status
    @GetMapping("/customer/{customerId}/score")
    public ResponseEntity<ApiResponse<CibilScoreResponse>> getCibilScore(
            @PathVariable Long customerId) {

        log.info(
                "Fetching CIBIL score for customerId={}",
                customerId
        );

        CibilScoreResponse response =
                cibilService.getLatestCibilScore(customerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "CIBIL score fetched successfully",
                        response
                )
        );
    }
}