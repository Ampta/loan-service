package com.ampta.dto.response;

import com.ampta.entity.enums.RiskCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCardResponse {

    private Integer scoreCardId;

    private Long customerId;
    private String customerName;
    private String panNumber;

    private Double monthlyIncome;
    private Double monthlySpending;

    private String currentStatus;
    private String rejectionReason;
    private LocalDateTime appliedDate;

    private Integer cibilScore;
    private RiskCategory riskCategory;
    private Double eligibleLoanAmount;
}