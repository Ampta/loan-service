
package com.ampta.config;

import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.response.LoanApplicationResponse;
import com.ampta.entity.LoanDeal;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanDeal toEntity(LoanApplicationRequest request) {

        return LoanDeal.builder()
                .loanType(request.getLoanType())
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .emiDay(request.getEmiDay())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .ifscCode(request.getIfscCode())
                .build();
    }

    public LoanApplicationResponse toResponse(LoanDeal loanDeal) {

        return LoanApplicationResponse.builder()
                .dealId(loanDeal.getDealId())
                .customerId(
                        loanDeal.getCustomer() != null
                                ? loanDeal.getCustomer().getCustomerId()
                                : null
                )
                .loanType(loanDeal.getLoanType())
                .loanAmount(loanDeal.getLoanAmount())
                .interestRate(loanDeal.getInterestRate())
                .tenureMonths(loanDeal.getTenureMonths())
                .emiAmount(loanDeal.getEmiAmount())
                .emiDay(loanDeal.getEmiDay())
                .bankName(loanDeal.getBankName())
                .bankAccountNumber(loanDeal.getBankAccountNumber())
                .ifscCode(loanDeal.getIfscCode())
                .status(loanDeal.getStatus())
                .build();
    }
}
