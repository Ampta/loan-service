
package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanctionLetterRequest {

    @NotNull(message = "Loan deal ID is required")
    private Long dealId;
}
