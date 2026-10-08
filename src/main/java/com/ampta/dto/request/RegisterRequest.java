package com.ampta.dto.request;

import com.ampta.entity.enums.EmployeeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 4, message = "First name must contains 4 characters or more")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 4, message = "Last name must contains 4 characters or more")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian phone number")
    private String phoneNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format (e.g., ABCDE1234F)")
    private String panNumber;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Aadhaar number must be exactly 12 numeric digits and cannot start with 0 or 1")
    private String aadhaarNumber;

    @NotNull(message = "Employee type is required")
    private EmployeeType employeeType;

    @NotNull(message = "Monthly earning is required")
    @PositiveOrZero(message = "Monthly earning cannot be negative")
    private Double monthlyEarning;

    @NotNull(message = "Monthly spending is required")
    @PositiveOrZero(message = "Monthly spending cannot be negative")
    private Double monthlySpending;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;
}
