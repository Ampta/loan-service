package com.ampta.service;

import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.response.LoanPaymentResponse;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;

public interface LoanPaymentService {
    LoanPaymentResponse makePayment(@Valid LoanPaymentRequest request) throws BadRequestException;
}
