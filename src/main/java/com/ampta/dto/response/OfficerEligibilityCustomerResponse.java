package com.ampta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerEligibilityCustomerResponse {

    private Long customerId;
    private String customerName;
    private String email;
    private String phoneNumber;

    private Integer cibilScore;
    private Double monthlyEarning;
    private Double monthlySpending;

    private Long eligibilityId;
    private String decisionStatus;
}