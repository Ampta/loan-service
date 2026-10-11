package com.ampta.entity;

import com.ampta.entity.enums.EmployeeType;
import com.ampta.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    private String lastName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", unique = true)
    private Customer customer;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String refreshToken;
    private Instant refreshTokenExpiry;

    private Boolean mfaEnabled = false;
    private String mfaSecret;

    private String emailOtp;
    private Instant emailOtpExpiry;

    private String passwordResetToken;
    private Instant passwordResetTokenExpiry;

}