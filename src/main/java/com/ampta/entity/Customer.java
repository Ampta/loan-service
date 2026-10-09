package com.ampta.entity;

import com.ampta.entity.enums.EmployeeType;
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
public class Customer extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String aadhaarNumber;
    private String panNumber;
    private EmployeeType employeeType;
    private Double monthlyEarning;
    private Double monthlySpending;
    private Integer age;
    private String address;
    private Boolean isEmailVerified = false;
    private String verificationToken;
    private String accountStatus = "ACTIVE";

}
