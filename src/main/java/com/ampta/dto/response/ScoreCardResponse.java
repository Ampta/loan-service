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
public class ScoreCardResponse {

    private Long scoreCardId;
    private Long customerId;
    private String customerName;
    private String currentStatus;
    private String rejectionReason;
    private LocalDateTime appliedDate;
    private Integer cibilScore;
    private String riskCategory;
    private Double eligibleLoanAmount;

}
