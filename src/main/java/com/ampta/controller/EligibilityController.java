package com.ampta.controller;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.request.EligibilityDecisionRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.service.EligibilityService;
import com.ampta.utils.Endpoints;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Endpoints.V1_ELIGIBILITY)
public class EligibilityController {

    private final EligibilityService eligibilityService;

    // Customers with CIBIL score below 800.
    @GetMapping("/officer/manual-review/customers")
    public ResponseEntity<
            ApiResponse<List<OfficerEligibilityCustomerResponse>>
            > getManualReviewCustomers(
            ) {

        List<OfficerEligibilityCustomerResponse> response =
                eligibilityService.getManualReviewCustomers();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Manual-review customers fetched successfully",
                        response
                )
        );
    }

    // Check eligibility for selected customer.
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    checkEligibility(
            @Valid @RequestBody EligibilityCheckRequest request,
            @RequestParam Long officerId) {

        log.info(
                "Checking eligibility for customer: {}",
                request.getCustomerId()
        );

        EligibilityResultResponse response =
                eligibilityService.checkEligibilityForOfficer(
                        request,
                        officerId
                );

        return new ResponseEntity<>(
                new ApiResponse<>(
                        true,
                        "Eligibility checked successfully",
                        response
                ),
                HttpStatus.OK
        );
    }

    // Officer approves or rejects a pending result.
    @PutMapping("/officer/{eligibilityId}/decision")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    decideEligibility(
            @PathVariable Long eligibilityId,
            @RequestParam Long officerId,
            @Valid @RequestBody EligibilityDecisionRequest request) {

        EligibilityResultResponse response =
                eligibilityService.decideEligibility(
                        eligibilityId,
                        officerId,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Eligibility decision saved successfully",
                        response
                )
        );
    }

    @GetMapping("/{eligibilityId}")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    getEligibilityById(@PathVariable Long eligibilityId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Eligibility result fetched successfully",
                        eligibilityService.getEligibilityById(eligibilityId)
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    getCustomerEligibility(@PathVariable Long customerId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer eligibility fetched successfully",
                        eligibilityService.getCustomerEligibility(customerId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EligibilityResultResponse>>>
    getAllEligibility() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Eligibility results fetched successfully",
                        eligibilityService.getAllEligibility()
                )
        );
    }
}