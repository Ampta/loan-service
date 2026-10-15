package com.ampta.controller;


import com.ampta.dto.request.LoginRequest;
import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.AuthResponse;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;
import com.ampta.service.AuthService;
import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Endpoints.V1_AUTH)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterRequest request){
        log.info("REST request to register user with email: {}", request);

        UserResponse response = authService.register(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "User register successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("REST request to verify email with token");
        authService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully. You can now log in.", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmailPost(@RequestParam String token) {
        log.info("REST request to verify email with token (POST)");
        authService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully. You can now log in.", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        log.info("REST request to resend verification email for: {}", email);
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Verification email resent successfully.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@Valid @RequestBody LoginRequest request){
        log.info("REST request to login user with email: {}", request);

        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, "User login successfully", response));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<AuthResponse>> verify2fa(@RequestParam String preAuthToken, @RequestParam String otpCode){
        log.info("REST request to verify 2fa");

        AuthResponse response = authService.verify2Fa(preAuthToken, otpCode);
        return ResponseEntity.ok(new ApiResponse<>(true, "2FA verified successfully", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken){
        log.info("REST request to refresh token");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", response));
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<ApiResponse<Void>> revokeToken(@RequestParam String refreshToken) {
        log.info("REST request to revoke token");
        authService.revokeToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>(true, "Refresh token revoke successfully", null));
    }

    @PostMapping("/lost-2fa/request")
    public ResponseEntity<ApiResponse<Void>> requestLost2Fa(@RequestParam String email) {
        log.info("REST request to lost 2fa with email: {}", email);
        authService.requestLost2Fa(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lost 2fa request send successfully", null));
    }

    @PostMapping("/lost-2fa/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmLost2Fa(@RequestParam String email, @RequestParam String emailOtp) {
        log.info("REST request to lost 2fa confirm with email: {}", email);
        authService.confirmLost2Fa(email, emailOtp);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lost 2fa confirmed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("REST request to forget password with email: {}", email);
        authService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Forget password request send successfully", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String email,
            @RequestParam String resetToken,
            @RequestParam String newPassword
    ) {
        log.info("REST request to reset password with email: {}", email);
        authService.resetPassword(email, resetToken, newPassword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password rest successfully", null));
    }

}
