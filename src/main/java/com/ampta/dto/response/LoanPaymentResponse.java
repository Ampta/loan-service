package com.ampta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanPaymentResponse {

    private Long paymentId;
    private Long loanAccountId;
    private Long emiScheduleId;

    private Double paymentAmount;

    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String paymentName;
    private String paymentMode;
}