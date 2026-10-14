
package com.ampta.controller;

import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.DisbursementResponse;
import com.ampta.service.DisbursementService;

import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoints.V1_DISBURSEMENT)
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;

    @PostMapping
    public ResponseEntity<ApiResponse<DisbursementResponse>> disburseLoan(
            @Valid @RequestBody DisbursementRequest request) {

        DisbursementResponse disburseLoan=
                disbursementService.disburseLoan(request);

        ApiResponse<DisbursementResponse> apiResponse=ApiResponse.<DisbursementResponse>builder()
                .success(true)
                .data(disburseLoan)
                .message("Loan Disbursement Successfully in Customer Account")
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<DisbursementResponse>>> fetchAllDisbursement()
    {
        List<DisbursementResponse> list=disbursementService.getAllDisbursement();

        ApiResponse<List<DisbursementResponse>> apiResponse=ApiResponse.<List<DisbursementResponse>>builder()
                .success(true)
                .message("Fetch All Disbursement Successfully")
                .data(list)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
