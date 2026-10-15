package com.ampta.entity;

import com.ampta.entity.enums.EmployeeType;
import com.ampta.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", length = 255)
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Pattern(regexp = "^[0-9]{12}$", message = "Invalid Aadhaar number format")
    @Column(name = "aadhaar_number", unique = true, length = 12)
    private String aadhaarNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    @Column(name = "pan_number", unique = true, length = 10)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", length = 30)
    private EmployeeType employeeType;

    @PositiveOrZero(message = "Monthly earning must be zero or positive")
    @Column(name = "monthly_earning")
    private Double monthlyEarning;

    @PositiveOrZero(message = "Monthly spending must be zero or positive")
    @Column(name = "monthly_spending")
    private Double monthlySpending;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 120, message = "Invalid age")
    @Column(name = "age")
    private Integer age;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @JsonIgnore
    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private Instant verificationTokenExpiry;

    @Column(name = "account_status", nullable = false, length = 30)
    @Builder.Default
    private String accountStatus = "PENDING_VERIFICATION";

    @Enumerated(EnumType.STRING)
    private Role role;

}
