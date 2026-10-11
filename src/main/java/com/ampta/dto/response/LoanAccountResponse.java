
package com.ampta.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountResponse {

    private Long loanAccountId;

    private Long customerId;

    private Long dealId;

    private String loanAccountNo;

    private Double loanAmount;

    private Double outstandingPrincipal;

    private String loanStatus;

    private Double interestRate;

    private Integer tenureMonths;

    private Double emiAmount;

    private LocalDateTime disbursementDate;

    private Double totalPaidAmount;

    private LocalDateTime createdAt;
}
