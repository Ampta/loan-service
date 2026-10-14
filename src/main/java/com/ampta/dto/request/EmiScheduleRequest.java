package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmiScheduleRequest {

    @NotNull(message = "Loan account ID is required")
    private Long loanAccountId;
}
