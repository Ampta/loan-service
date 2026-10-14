
package com.ampta.entity;

import com.ampta.entity.enums.EmployeeType;
import com.ampta.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    @Column(name = "pan_number")
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type")
    private EmployeeType employeeType;

    @Column(name = "monthly_earning")
    private Double monthlyEarning;

    @Column(name = "monthly_spending")
    private Double monthlySpending;

    private Integer age;

    private String address;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "account_status")
    private String accountStatus = "ACTIVE";

    @Enumerated(EnumType.STRING)
    private Role role;
}
