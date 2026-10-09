package com.ampta.dto.request;

import com.ampta.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReviewRequest {

    @NotNull(message = "Review status is required")
    private LoanStatus status;
}
