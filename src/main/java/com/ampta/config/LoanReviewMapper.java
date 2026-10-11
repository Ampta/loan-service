package com.ampta.config;

import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.entity.DealReview;
import org.springframework.stereotype.Component;

@Component
public class LoanReviewMapper {

    public LoanReviewResponse toResponse(DealReview dealReview) {

        LoanReviewResponse response = new LoanReviewResponse();

        if (dealReview.getLoanDeal() != null) {
            response.setDealId(
                    dealReview.getLoanDeal().getDealId()
            );

            response.setLoanStatus(
                    dealReview.getLoanDeal().getStatus()
            );
        }

        if (dealReview.getOfficer() != null) {
            response.setOfficerId(
                    dealReview.getOfficer().getUserId()
            );
        }

        return response;
    }
}