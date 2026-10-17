package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.SanctionLetterResponse;
import com.ampta.entity.SanctionLetter;
import com.ampta.service.SanctionLetterService;

import com.ampta.utils.Endpoints;
import lombok.RequiredArgsConstructor;

import org.modelmapper.internal.bytebuddy.build.EntryPoint;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Endpoints.V1_SANCTION_LETTER)
@RequiredArgsConstructor
public class SanctionLetterController {

    private final SanctionLetterService sanctionLetterService;

    // Generate sanction letter
    @PostMapping("/generate/{dealId}")
    public ResponseEntity<ApiResponse<SanctionLetterResponse>> generateSanctionLetter(
            @PathVariable Long dealId) {

        SanctionLetterResponse letterResponse =
                sanctionLetterService.generateSanctionLetter(dealId);

        ApiResponse<SanctionLetterResponse> apiResponse=ApiResponse.<SanctionLetterResponse>builder()
                .success(true)
                .data(letterResponse)
                .message("Successfully Download Sanction Letter")
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    // Get sanction letter details
    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<SanctionLetterResponse>> getSanctionLetter(
            @PathVariable Long dealId) {

        SanctionLetterResponse sanctionLetter=sanctionLetterService.generateSanctionLetter(dealId);

        ApiResponse<SanctionLetterResponse> apiResponse=ApiResponse.<SanctionLetterResponse>builder()
                .success(true)
                .data(sanctionLetter)
                .message("Successfully fetch the sanction Letter")
                .build();

        return ResponseEntity.ok(apiResponse);


    }

    // Download sanction letter PDF
    @GetMapping("/{dealId}/download")
    public ResponseEntity<Resource> downloadSanctionLetter(
            @PathVariable Long dealId) {

        return sanctionLetterService.downloadSanctionLetter(dealId);
    }
}
