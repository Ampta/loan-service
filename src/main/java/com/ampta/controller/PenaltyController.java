package com.ampta.controller;

import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.PenaltyChargeResponse;
import com.ampta.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/penalties")
public class PenaltyController {
    private final PenaltyService penaltyService;

    @GetMapping("/loan-account/{loanAccountId}/schedules/{emiScheduleId}/penalties")
    public ResponseEntity<ApiResponse<List<PenaltyChargeResponse>>> getEmiPenalties(
            @PathVariable Long loanAccountId,
            @PathVariable Long emiScheduleId) {
        log.info("REST request to get penalties for loanAccountId: {}, emiScheduleId: {}", loanAccountId, emiScheduleId);

        List<PenaltyChargeResponse> response = penaltyService.getEmiPenalties(loanAccountId, emiScheduleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMI penalties retrieved successfully", response )
        );
    }
}
