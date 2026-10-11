package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanPaymentRequest {

    @NotNull(message = "Loan Account ID is required")
    private Long loanAccountId;

    private Long emiScheduleId;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private Double paymentAmount;

    private String paymentName = "EMI_PAYMENT";

    private String paymentMode = "ONLINE";

    private Boolean deductFromWallet = false;
}