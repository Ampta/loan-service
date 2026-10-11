package com.ampta.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanPaymentResponse {
    private Integer paymentId;
    private Integer loanAccountId;
    private Integer emiScheduleId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String paymentName;
    private String paymentMode;
}
