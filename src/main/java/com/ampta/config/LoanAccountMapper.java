
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

        if (account.getLoanDeal() != null) {
            response.setDealId(
                    account.getLoanDeal().getDealId()
            );
        }

        if (account.getCustomer() != null) {
            response.setCustomerId(
                    account.getCustomer().getCustomerId()
            );
        }

        return response;
    }
}
