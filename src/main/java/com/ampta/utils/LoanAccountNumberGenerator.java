package com.ampta.utils;

import org.springframework.stereotype.Component;

@Component
public class LoanAccountNumberGenerator {

    public String generate(Long loanAccountId)
    {
        if(loanAccountId == null || loanAccountId < 0)
        {
            throw new IllegalArgumentException("Valid Loan Id is Required :"+ loanAccountId);
        }

        return String.format("LA%06d",loanAccountId);
    }
}
