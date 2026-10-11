package com.ampta.controller;

import com.ampta.dto.request.EmiPaymentStatusRequest;
import com.ampta.dto.request.EmiScheduleRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.EmiScheduleResponse;
import com.ampta.dto.response.PenaltyChargeResponse;
import com.ampta.service.EmiScheduleService;
import com.ampta.service.PenaltyService;
import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(Endpoints.V1_SCHEDULER)
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;
    private final PenaltyService penaltyService;

    @PostMapping
    public ResponseEntity<List<EmiScheduleResponse>> createEmiSchedule(
            @Valid @RequestBody EmiScheduleRequest request) {
        log.info("Creating EMI schedule for loan account: {}", request.getLoanAccountId());
        List<EmiScheduleResponse> response = emiScheduleService.createEmiSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/loan-account/{loanAccountId}")
    public ResponseEntity<Page<EmiScheduleResponse>> getEmiSchedulesByLoanAccountId(
            @PathVariable Long loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be >= 1");
        }
        Page<EmiScheduleResponse> response = emiScheduleService.getEmiSchedulesByLoanAccountId(loanAccountId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }


    // Get all EMI schedule IDs by Loan Account ID
    @GetMapping("/loan-account/{loanAccountId}/schedule-ids")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>>
    getEmiScheduleIdsByLoanAccountId(
            @PathVariable Long loanAccountId) {
        List<EmiScheduleResponse> scheduleIds = emiScheduleService.getEmiScheduleDetailsByLoanAccountId(loanAccountId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "EMI schedule IDs retrieved successfully",
                        scheduleIds
                )
        );
    }


    @PatchMapping("/{emiScheduleId}/payment-status")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>>
    updatePaymentStatus(@PathVariable Long emiScheduleId, @Valid @RequestBody EmiPaymentStatusRequest request) {
        EmiScheduleResponse response = emiScheduleService.updatePaymentStatus(emiScheduleId, request);
        return ResponseEntity.ok(new ApiResponse<>(
                true, "EMI payment status updated successfully", response));
    }



    @GetMapping("/loan-account/{loanAccountId}/schedules/{emiScheduleId}/penalties")
    public ResponseEntity<ApiResponse<List<PenaltyChargeResponse>>>
    getEmiPenalties(
            @PathVariable Long loanAccountId,
            @PathVariable Long emiScheduleId) {
        List<PenaltyChargeResponse> response = penaltyService.getEmiPenalties(
                loanAccountId, emiScheduleId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "EMI penalties retrieved successfully",
                        response
                )
        );
    }
}