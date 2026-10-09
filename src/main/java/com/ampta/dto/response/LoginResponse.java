package com.ampta.dto.response;

import com.ampta.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private Role role;
    private Long customerId;
    private String firstName;
    private String lastName;
}
