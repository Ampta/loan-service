package com.ampta.service;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.AuthResponse;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    void verifyEmail(String token);
    void resendVerificationEmail(String email);
    LoginResponse login(String email, String password);
    AuthResponse verify2Fa(String preAuthToken, String otpCode);
    AuthResponse refreshToken(String refreshToken);
    void revokeToken(String refreshToken);
    void requestLost2Fa(String email);
    void confirmLost2Fa(String email, String emailOtp);
    void forgotPassword(String email);
    void resetPassword(String email, String resetToken, String newPassword);
}

