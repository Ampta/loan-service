package com.ampta.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ClosureCreateRequest(

        @NotNull
        @Positive
        Long loanAccountId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal paymentAmount

) {
}