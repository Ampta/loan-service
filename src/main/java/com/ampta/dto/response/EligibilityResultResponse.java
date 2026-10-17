package com.ampta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EligibilityResultResponse {

    private Long eligibilityId;

    private Long customerId;
    private String customerName;
    private String email;
    private String phoneNumber;

    private Integer cibilScore;
    private Double monthlyEarning;
    private Double monthlySpending;

    private Boolean isEligible;
    private Double loanAmount;
    private String rejectionReason;

    // Calculated in Java, not stored in the database.
    private String decisionStatus;

    private Instant createdAt;
}