package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "FirstName", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LastName", length = 100)
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "Password", length = 255)
    private String password;

    @Column(name = "MobileNo", length = 20)
    private String mobileNo;

    @Column(name = "AadhaarNo", length = 30)
    private String aadhaarNo;

    @Column(name = "PanNo", length = 20)
    private String panNo;

    @Column(name = "EmploymentType", length = 100)
    private String employmentType; // Government, Private sector, Personal

    @Column(name = "MonthlyIncome", precision = 18, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "ExistingMonthlyEmi", precision = 18, scale = 2)
    private BigDecimal existingMonthlyEmi = BigDecimal.ZERO;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "Address", length = 500)
    private String address;

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "VerificationToken", length = 255)
    private String verificationToken;

    @Column(name = "AccountStatus", length = 50)
    private String accountStatus = "ACTIVE"; // ACTIVE, DEACTIVATED, SUSPENDED

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
