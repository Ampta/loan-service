package com.ampta.service;

import com.ampta.dto.request.LoanReviewRequest;
import com.ampta.dto.response.LoanReviewResponse;

public interface LoanReviewService {

    LoanReviewResponse reviewLoan(
            Long dealId,
            Long officerId,
            LoanReviewRequest request
    );
}