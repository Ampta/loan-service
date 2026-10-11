package com.ampta.dto.response;

import com.ampta.entity.LoanStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReviewResponse {

    private Long reviewId;

    private Long dealId;

    private Long officerId;

    private LoanStatus loanStatus;

    private String message;
}