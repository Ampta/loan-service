package com.ampta.dto.response;

import com.ampta.entity.enums.EmployeeType;
import com.ampta.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String panNumber;
    private String aadhaarNumber;
    private EmployeeType employeeType;
    private Double monthlyEarning;
    private Double monthlySpending;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;
}
