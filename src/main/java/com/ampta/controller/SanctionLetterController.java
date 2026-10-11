package com.ampta.controller;

import com.ampta.dto.response.SanctionLetterResponse;
import com.ampta.service.SanctionLetterService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sanction-letters")
@RequiredArgsConstructor
public class SanctionLetterController {

    private final SanctionLetterService sanctionLetterService;

    // Generate sanction letter
    @PostMapping("/generate/{dealId}")
    public ResponseEntity<SanctionLetterResponse> generateSanctionLetter(
            @PathVariable Long dealId) {

        SanctionLetterResponse response =
                sanctionLetterService.generateSanctionLetter(dealId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get sanction letter details
    @GetMapping("/{dealId}")
    public ResponseEntity<SanctionLetterResponse> getSanctionLetter(
            @PathVariable Long dealId) {

        return ResponseEntity.ok(
                sanctionLetterService.getSanctionLetter(dealId));
    }

    // Download sanction letter PDF
    @GetMapping("/{dealId}/download")
    public ResponseEntity<Resource> downloadSanctionLetter(
            @PathVariable Long dealId) {

        return sanctionLetterService.downloadSanctionLetter(dealId);
    }
}
