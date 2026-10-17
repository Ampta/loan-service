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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Endpoints.V1_ELIGIBILITY)
public class EligibilityController {

    private final EligibilityService eligibilityService;

    // 1. Fetch customers who need manual eligibility review.
//    @PreAuthorize("hasRole('OFFICER')")
    @GetMapping("/officer/manual-review/customers")
    public ResponseEntity<
            ApiResponse<List<OfficerEligibilityCustomerResponse>>>
    getManualReviewCustomers() {

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

    // 2. Check eligibility for the selected customer.
//    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping("/customers/{customerId}/check")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    checkEligibility(@PathVariable Long customerId) {

        log.info("Checking eligibility for customer: {}", customerId);

        EligibilityResultResponse response =
                eligibilityService.checkEligibility(customerId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Eligibility checked successfully",
                        response
                )
        );
    }

    // 3. Approve or reject a pending eligibility result.
//    @PreAuthorize("hasRole('OFFICER')")
    @PutMapping("/officer/{eligibilityId}/decision")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>>
    decideEligibility(
            @PathVariable Long eligibilityId,
            @Valid @RequestBody EligibilityDecisionRequest request) {

        EligibilityResultResponse response =
                eligibilityService.decideEligibility(
                        eligibilityId,
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
}