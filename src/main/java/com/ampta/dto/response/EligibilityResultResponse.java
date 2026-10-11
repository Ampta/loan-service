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
    private Integer cibilScore;
    private Boolean isEligible;
    private Double loanAmount;
    private String rejectionReason;
    private Instant createdAt;
}
