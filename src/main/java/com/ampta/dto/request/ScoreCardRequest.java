package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreCardRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;
    private String currentStatus;
    private String rejectionReason;
    private Integer cibilScore;
    private String riskCategory;
    private Double eligibleLoanAmount;
}