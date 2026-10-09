package com.ampta.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CibilCheckRequest {

    @NotBlank(message = "PAN number is required")
    @Size(
            max = 20,
            message = "PAN number cannot exceed 20 characters"
    )
    @Pattern(
            regexp = "[A-Z]{5}[0-9]{4}[A-Z]",
            message = "Invalid PAN number"
    )
    private String panNumber;

    @NotNull(message = "Age is required")
    @Min(
            value = 21,
            message = "Age must be between 21 and 60"
    )
    @Max(
            value = 60,
            message = "Age must be between 21 and 60"
    )
    private Integer age;

    @NotNull(message = "Total monthly debt payment is required")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Total monthly debt payment cannot be negative"
    )
    @NotNull(message = "Monthly spending is required")
    @PositiveOrZero(message = "Monthly spending cannot be negative")
    private Double monthlySpending;
}