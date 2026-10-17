
package com.ampta.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementResponse {

    private Long disbursementId;

    private Long dealId;

    private Double disburseAmount;

    private String bankPartner;

    private LocalDateTime disbursementDate;

    private String status;

    private String message;

    private String loanAccountNo;
}
