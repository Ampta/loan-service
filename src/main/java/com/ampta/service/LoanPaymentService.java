package com.ampta.service;

import com.ampta.dto.request.LoanPaymentRequest;
import com.ampta.dto.response.LoanPaymentResponse;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanPayment;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;

public interface LoanPaymentService {
    LoanPaymentResponse makePayment(@Valid LoanPaymentRequest request) throws BadRequestException;
    LoanPayment recordNonEmiPayment(
            LoanAccount account,
            Double amount,
            String paymentName,
            String paymentMode
    );
}
