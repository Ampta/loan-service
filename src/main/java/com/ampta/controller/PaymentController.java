package com.ampta.controller;
import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.LoanPaymentResponse;
import com.ampta.service.LoanPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
