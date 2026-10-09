package com.ampta.dto.request;

import com.ampta.entity.LoanStatus;
import com.ampta.entity.LoanType;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {

    private Long dealId;

    private Long userId;

    private LoanType loanType;

    private Double loanAmount;

    private Double interestRate;

    private Integer tenureMonths;

    private Double emiAmount;

    private Integer emiDay;

    private LoanStatus status;
}
