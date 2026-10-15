package com.ampta.controller;
import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.request.PaymentStatusRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.LoanPaymentResponse;
import com.ampta.service.LoanPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final LoanPaymentService loanPaymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> makePayment(@Valid @RequestBody LoanPaymentRequest request) throws BadRequestException {
        log.info("REST request to make payment for loan account: {}", request.getLoanAccountId());
        LoanPaymentResponse response = loanPaymentService.makePayment(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Payment processed successfully", response), HttpStatus.CREATED);
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> getPaymentById(@PathVariable Long paymentId) {
        log.info("REST request to get payment: {}", paymentId);
        LoanPaymentResponse response = loanPaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanPaymentResponse>>> getAllPayments() {
        log.info("REST request to get All  payments");
        List<LoanPaymentResponse> allPayment = loanPaymentService.getAllPayment();
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", allPayment));
    }


    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentStatusRequest request) {
        log.info("REST request to update payment status: {}", paymentId);
        LoanPaymentResponse response = loanPaymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment status updated successfully", response));
    }

}

