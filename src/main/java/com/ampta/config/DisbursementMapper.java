package com.ampta.config;

import com.ampta.dto.response.DisbursementResponse;
import com.ampta.entity.Disbursement;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class DisbursementMapper {

    private final ModelMapper modelMapper;

    public DisbursementMapper(ModelMapper modelMapper)
    {
        this.modelMapper=modelMapper;
    }

    public DisbursementResponse toResponse(Disbursement disbursement)
    {
        DisbursementResponse response=
                modelMapper.map(disbursement,DisbursementResponse.class);

        response.setDealId(disbursement.getLoanDeal().getDealId());

        return response;
    }
}
