
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
import org.springframework.data.domain.Pageable;
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

    //create EmoSchedule
    @PostMapping
    public ResponseEntity<List<EmiScheduleResponse>>
    createEmiSchedule(@RequestBody EmiScheduleRequest request) {
        log.info("REST request to create EMI schedule for loan account: {}", request.getLoanAccountId());
        List<EmiScheduleResponse> response = emiScheduleService.createEmiSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //Get All EMIs
    @GetMapping("/loan-account/{loanAccountId}")
    public ResponseEntity<Page<EmiScheduleResponse>> getEmiSchedulesByLoanAccountId(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EmiScheduleResponse> response = (Page<EmiScheduleResponse>) emiScheduleService.getEmiSchedulesByLoanAccountId(loanAccountId, (Pageable) PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    //get Specific scheduledEMi
    @GetMapping("/{emiScheduleId}")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> getEmiScheduleById(@PathVariable Integer emiScheduleId) {
        log.info("REST request to get EMI schedule by id: {}", emiScheduleId);
        EmiScheduleResponse response = emiScheduleService.getEmiScheduleById(emiScheduleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMI schedule retrieved successfully", response));
    }


    @PatchMapping("/{emiScheduleId}/payment-status")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> updatePaymentStatus(@PathVariable Integer emiScheduleId, @Valid @RequestBody EmiPaymentStatusRequest request) {
        log.info("REST request to update payment status for EMI schedule: {}", emiScheduleId);
        EmiScheduleResponse response = emiScheduleService.updatePaymentStatus(emiScheduleId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMI schedule payment status updated successfully", response));
    }


    @GetMapping("/{emiScheduleId}/penalties")
    public ResponseEntity<ApiResponse<List<PenaltyChargeResponse>>> getEmiPenalties(@PathVariable Integer emiScheduleId) {
        log.info("REST request to get penalties for EMI schedule: {}", emiScheduleId);
        List<PenaltyChargeResponse> response = penaltyService.getEmiPenalties(emiScheduleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMI penalties retrieved successfully", response));
    }


}