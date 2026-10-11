package com.ampta.config;

import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.entity.DealReview;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoanReviewMapper {

    private final ModelMapper modelMapper;

    public LoanReviewMapper(ModelMapper modelMapper)
    {
        this.modelMapper=modelMapper;
    }

    public LoanReviewResponse toResponse(DealReview dealReview)
    {
        LoanReviewResponse response=
                modelMapper.map(dealReview,LoanReviewResponse.class);

        response.setDealId(dealReview.getLoanDeal().getDealId());

        response.setOfficerId(dealReview.getOfficer().getUserId());

        response.setLoanStatus(dealReview.getLoanDeal().getStatus());

        return  response;
    }
}
