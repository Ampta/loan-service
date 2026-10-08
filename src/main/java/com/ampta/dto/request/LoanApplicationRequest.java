package com.ampta.dto.request;


import com.ampta.entity.LoanType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(
            value = "10000.00",
            message = "Loan amount must be at least 10000"
    )
    private Double loanAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    private Integer tenureMonths;

    @NotNull(message = "EMI day is required")
    @Min(value = 1, message = "EMI day must be between 1 and 28")
    @Max(value = 28, message = "EMI day must be between 1 and 28")
    private Integer emiDay;
}