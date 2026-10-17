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
public class PenaltyChargeResponse {
    private Long penaltyChargeId;
    private Long emiScheduleId;
    private Long loanAccountId;
    private Double bounceCharge;
    private Double dailyPenalty;
    private Integer overdueDays;
    private Double penaltyAmount;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
