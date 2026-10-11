package com.ampta.dto.request;

import com.ampta.entity.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

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

    @NotBlank(message = "Bank name is required")
    @Size(max = 200, message = "Bank name cannot exceed 200 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number cannot exceed 50 characters")
    private String bankAccountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(
            regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$",
            message = "Invalid IFSC code format"
    )
    private String ifscCode;
}
