
package com.ampta.config;

import com.ampta.dto.response.LoanAccountResponse;
import com.ampta.entity.LoanAccount;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoanAccountMapper {

    private final ModelMapper modelMapper;

    public LoanAccountMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public LoanAccountResponse toResponse(LoanAccount account) {

        LoanAccountResponse response =
                modelMapper.map(account, LoanAccountResponse.class);

        response.setDealId(account.getLoanDeal().getDealId());

        response.setCustomerId(account.getCustomer().getUserId());

        return response;
    }
}
