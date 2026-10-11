package com.ampta.dto.response;

import com.ampta.entity.enums.LoanStatus;
import com.ampta.entity.enums.LoanType;
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

    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;

    private LoanStatus status;
}
