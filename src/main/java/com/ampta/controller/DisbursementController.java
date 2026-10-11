
package com.ampta.controller;

import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.DisbursementResponse;
import com.ampta.service.DisbursementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disbursements")
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;

    @PostMapping
    public ResponseEntity<DisbursementResponse> disburseLoan(
            @Valid @RequestBody DisbursementRequest request) {

        DisbursementResponse response =
                disbursementService.disburseLoan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
