package com.ampta.service;

import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.request.PaymentStatusRequest;
import com.ampta.dto.response.LoanPaymentResponse;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface LoanPaymentService {
    LoanPaymentResponse makePayment(@Valid LoanPaymentRequest request) throws BadRequestException;
    LoanPaymentResponse getPaymentById(Long paymentId);
    List<LoanPaymentResponse> getAllPayment();
    LoanPaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusRequest request);
}
