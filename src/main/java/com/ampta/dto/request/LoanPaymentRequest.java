package com.ampta.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanPaymentRequest {
    @NotNull(message = "Loan Account ID is required")
    private Integer loanAccountId;

    private Integer emiScheduleId;

    @NotNull(message = "Payment amount is required")
    private BigDecimal paymentAmount;

    private String paymentName = "EMI_PAYMENT"; // EMI_PAYMENT, FORECLOSURE_PAYMENT, PARTIAL_PAYMENT, PENALTY_PAYMENT
    private String paymentMode = "ONLINE"; // WALLET, NET_BANKING, UPI, CARD, CASH
    private Boolean deductFromWallet = false;
}