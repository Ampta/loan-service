package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EligibilityCheckRequest {
//    @NotNull(message = "Customer ID is required")
//    private Long customerId;
//    private Integer tenureMonths = 36;
//    private Double interestRate;

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

}
