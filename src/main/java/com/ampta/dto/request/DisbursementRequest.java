
package com.ampta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementRequest {

    @NotNull(message = "Deal ID is required")
    private Long dealId;

    @NotBlank(message = "Bank partner is required")
    private String bankPartner;

    @NotNull(message = "Disbursement date is required")
    private LocalDate disbursementDate;
}
