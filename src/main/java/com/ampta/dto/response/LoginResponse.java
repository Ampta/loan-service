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
    private boolean mfaRequired;
    private boolean isFirstTimeSetup;
    private String preAuthToken;
    private String secretKey;
    private String qrCodeDataUri;
    private String message;
    private AuthResponse auth;
}
