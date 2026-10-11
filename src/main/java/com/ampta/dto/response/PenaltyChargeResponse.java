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
    private Integer penaltyChargeId;
    private Integer emiScheduleId;
    private Integer loanAccountId;
    private BigDecimal penaltyAmount;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
