package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EligibilityDecisionRequest {

    @NotNull(message = "Please select approve or reject")
    private Boolean approved;

    @Size(
            max = 1000,
            message = "Rejection reason cannot exceed 1000 characters"
    )
    private String rejectionReason;
}