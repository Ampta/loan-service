package com.ampta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ForeclosureCreateRequest(

        @NotNull
        @Positive
        Long loanAccountId

) {
}