
package com.ampta.dto.response;

import com.ampta.entity.enums.LoanStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanctionLetterResponse {

    private Long sanctionLetterId;
    private Long dealId;
    private String sanctionLetterNo;
    private Double sanctionedAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private Double emiAmount;
    private LocalDate sanctionDate;
    private LoanStatus status;
    private String documentPath;
    private String remarks;
}
