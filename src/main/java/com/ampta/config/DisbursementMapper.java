package com.ampta.config;

import com.ampta.dto.response.DisbursementResponse;
import com.ampta.entity.Disbursement;
import com.ampta.entity.LoanAccount;
import org.springframework.stereotype.Component;

@Component
public class DisbursementMapper {

    public DisbursementResponse toResponse(
            Disbursement disbursement,
            LoanAccount loanAccount) {

        return DisbursementResponse.builder()

                .disbursementId(
                        disbursement.getDisbursementId()
                )

                .dealId(
                        disbursement.getLoanDeal() != null
                                ? disbursement.getLoanDeal().getDealId()
                                : null
                )

                .disburseAmount(
                        disbursement.getDisburseAmount()
                )

                .bankPartner(
                        disbursement.getBankPartner()
                )

                .disbursementDate(
                        disbursement.getDisbursementDate()
                )

                .status(
                        String.valueOf(disbursement.getStatus())
                )

                .loanAccountNo(
                        loanAccount != null
                                ? loanAccount.getLoanAccountNo()
                                : null
                )

                .build();
    }
}
