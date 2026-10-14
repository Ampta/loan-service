package com.ampta.controller;


import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.service.EligibilityService;
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
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Endpoints.V1_ELIGIBILITY)
public class EligibilityController {

    private final EligibilityService eligibilityService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>> checkEligibility(@Valid @RequestBody EligibilityCheckRequest request) {
        log.info("REST request to check eligibility for customer: {}", request.getCustomerId());
        EligibilityResultResponse response = eligibilityService.checkEligibility(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Eligibility checked successfully", response), HttpStatus.OK);
    }
}
