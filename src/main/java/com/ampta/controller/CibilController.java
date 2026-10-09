package com.ampta.controller;

import com.ampta.dto.request.CibilCheckRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.CibilScoreResponse;
import com.ampta.service.CibilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cibil-reports")
@RequiredArgsConstructor
@Slf4j
public class CibilController {

    private final CibilService cibilService;

    // =====================================================
    // CALCULATE CIBIL SCORE
    // =====================================================

    @PostMapping("/customer/{customerId}/calculate")
    public ResponseEntity<ApiResponse<CibilScoreResponse>>
    calculateCibilScore(@PathVariable Long customerId) {

        log.info(
                "CIBIL score calculation requested for customerId={}",
                customerId
        );

        CibilScoreResponse response =
                cibilService.calculateCibilScore(customerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "CIBIL score calculated successfully",
                                response
                        )
                );
    }


    // =====================================================
    // GET LATEST CIBIL SCORE
    // =====================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CibilScoreResponse>>
    getLatestCibilScore(
            @PathVariable Long customerId) {

        log.info(
                "Fetching latest CIBIL score for customerId={}",
                customerId
        );

        CibilScoreResponse response =
                cibilService.getLatestCibilScore(customerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Latest CIBIL score fetched successfully",
                        response
                )
        );
    }
}