package com.ampta.service.impl;

import com.ampta.dto.request.RegisterRequest;
import com.ampta.dto.response.AuthResponse;
import com.ampta.dto.response.LoginResponse;
import com.ampta.dto.response.UserResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.enums.Role;
import com.ampta.entity.User;
import com.ampta.exception.ResourceAlreadyExistsException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.UserRepository;
import com.ampta.security.jwt.JwtUtils;
import com.ampta.security.totp.TotpService;
import com.ampta.service.AuthService;
import com.ampta.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TotpService totpService;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Override
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail().trim();
        String phoneNumber = request.getPhoneNumber().trim();
        String panNumber = request.getPanNumber().trim();
        String aadhaarNumber = request.getAadhaarNumber().trim();

        if(userRepository.existsByEmail(email)){
            throw new ResourceAlreadyExistsException("Email already exists: " + email);
        }

        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new ResourceAlreadyExistsException("Phone number already exists: " + phoneNumber);
        }

        if(customerRepository.existsByPanNumber(panNumber)){
            throw new ResourceAlreadyExistsException("Pan number already exists: " + panNumber);
        }

        if(customerRepository.existsByAadhaarNumber(aadhaarNumber)){
            throw new ResourceAlreadyExistsException("Aadhaar number already exists: " + aadhaarNumber);
        }

        String verificationToken = UUID.randomUUID().toString();
        Instant verificationTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .aadhaarNumber(request.getAadhaarNumber())
                .panNumber(request.getPanNumber())
                .employeeType(request.getEmployeeType())
                .monthlyEarning(request.getMonthlyEarning())
                .monthlySpending(request.getMonthlySpending())
                .age(request.getAge())
                .address(request.getAddress())
                .isEmailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(verificationTokenExpiry)
                .accountStatus("PENDING_VERIFICATION")
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // Todo: create wallet here

        User user = User.builder()
                .role(Role.CUSTOMER)
                .customer(savedCustomer)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(verificationTokenExpiry)
                .mfaEnabled(false)
                .build();
        User savedUser = userRepository.save(user);

        emailService.sendRegistrationSuccessEmail(
                savedCustomer.getEmail(),
                savedCustomer.getFirstName() + (savedCustomer.getLastName() != null ? " " + savedCustomer.getLastName() : ""),
                request.getPassword(),
                verificationToken
        );

        return UserResponse.builder()
                .userId(savedUser.getUserId())
                .role(savedUser.getRole())
                .customerId(savedCustomer.getCustomerId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .isEmailVerified(savedUser.getIsEmailVerified())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    @Override
    public LoginResponse login(String email, String password) {
        String cleanEmail = email.trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, password)
        );

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("user not found with email: " + cleanEmail));


        String preAuthToken = jwtUtils.generatePreAuthToken(user.getEmail());

        if (user.getMfaEnabled() == null || !user.getMfaEnabled()) {
            String secret = totpService.generateSecret();
            user.setMfaSecret(secret);
            userRepository.save(user);

            String qrCodeDataUri = totpService.getQrCodeDataUri(secret, user.getEmail());

            return LoginResponse.builder()
                    .mfaRequired(true)
                    .isFirstTimeSetup(true)
                    .preAuthToken(preAuthToken)
                    .secretKey(secret)
                    .qrCodeDataUri(qrCodeDataUri)
                    .message("Please scan the QR code with Google/Microsoft Authenticator and verify with your 6-digit OTP.")
                    .build();
        }

        return LoginResponse.builder()
                .mfaRequired(true)
                .isFirstTimeSetup(false)
                .preAuthToken(preAuthToken)
                .message("Please enter the 6-digit OTP code from your Authenticator app.")
                .build();
    }

    @Override
    public AuthResponse verify2Fa(String preAuthToken, String otpCode) {
        if (!jwtUtils.validateToken(preAuthToken)) {
            throw new RuntimeException("Pre-auth token has expired or is invalid. Please log in again.");
        }

        String tokenType = jwtUtils.getTokenTypeFromToken(preAuthToken);
        if (!"PRE_AUTH".equalsIgnoreCase(tokenType)) {
            throw new RuntimeException("Invalid token type for 2FA verification.");
        }

        String email = jwtUtils.getEmailFromToken(preAuthToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        boolean isValid = totpService.verifyCode(user.getMfaSecret(), otpCode);
        if (!isValid) {
            throw new RuntimeException("Invalid 6-digit OTP. Please check your Authenticator app and try again.");
        }

        if (user.getMfaEnabled() == null || !user.getMfaEnabled()) {
            user.setMfaEnabled(true);
        }

        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole());
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(jwtUtils.getJwtRefreshExpirationMs()));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        User user = userRepository.findByRefreshToken(refreshToken.trim())
                .orElseThrow(() -> new ResourceNotFoundException("invalid refresh token or revoke"));

        if(user.getRefreshTokenExpiry().isBefore(Instant.now())){
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        // Generate new access token
        String newAccessToken = jwtUtils.generateToken(user.getEmail(), user.getRole());

        // Refresh Token Rotation (RTR): issue new refresh token and invalidate old one
        String newRefreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(jwtUtils.getJwtRefreshExpirationMs()));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .build();
    }

    @Override
    public void revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        userRepository.findByRefreshToken(refreshToken.trim()).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            log.info("Refresh token revoked successfully for user: {}", user.getEmail());
        });
    }

    @Override
    public void requestLost2Fa(String email) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate random 6-digit confirmation code
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setEmailOtp(otp);
        user.setEmailOtpExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);

        emailService.send2FaRecoveryOtp(user.getEmail(), user.getFirstName(), otp);
        log.info("2FA recovery code sent to email: {}", user.getEmail());
    }

    @Override
    public void confirmLost2Fa(String email, String emailOtp) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getEmailOtp() == null || !user.getEmailOtp().equals(emailOtp.trim())) {
            throw new RuntimeException("Invalid 2FA recovery code.");
        }

        if (user.getEmailOtpExpiry() == null || user.getEmailOtpExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("2FA recovery code has expired. Please request a new one.");
        }

        // Reset 2FA configuration so user will be shown a new QR code on next login
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setEmailOtp(null);
        user.setEmailOtpExpiry(null);
        userRepository.save(user);

        log.info("2FA successfully reset for user: {}", user.getEmail());
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
        log.info("Password reset token generated and sent to: {}", user.getEmail());
    }

    @Override
    public void resetPassword(String email, String resetToken, String newPassword) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(resetToken.trim())) {
            throw new RuntimeException("Invalid password reset token.");
        }

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Password reset token has expired. Please request a new one.");
        }

        // Update password and clear reset token & revoke existing sessions
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Override
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required.");
        }

        String cleanToken = token.trim();
        Customer customer = customerRepository.findByVerificationToken(cleanToken).orElse(null);
        User user = userRepository.findByVerificationToken(cleanToken).orElse(null);

        if (customer == null && user == null) {
            throw new ResourceNotFoundException("Invalid or expired verification token.");
        }

        Instant now = Instant.now();

        if (customer != null) {
            if (customer.getVerificationTokenExpiry() != null && customer.getVerificationTokenExpiry().isBefore(now)) {
                throw new RuntimeException("Verification token has expired. Please request a new verification email.");
            }
            customer.setIsEmailVerified(true);
            customer.setAccountStatus("ACTIVE");
            customer.setVerificationToken(null);
            customer.setVerificationTokenExpiry(null);
            customerRepository.save(customer);

            if (user == null) {
                user = userRepository.findByCustomer_CustomerId(customer.getCustomerId()).orElse(null);
            }
        }

        if (user != null) {
            if (user.getVerificationTokenExpiry() != null && user.getVerificationTokenExpiry().isBefore(now)) {
                throw new RuntimeException("Verification token has expired. Please request a new verification email.");
            }
            user.setIsEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user);

            if (customer == null && user.getCustomer() != null) {
                customer = user.getCustomer();
                customer.setIsEmailVerified(true);
                customer.setAccountStatus("ACTIVE");
                customer.setVerificationToken(null);
                customer.setVerificationTokenExpiry(null);
                customerRepository.save(customer);
            }
        }

        log.info("Email verification completed successfully for token: {}", cleanToken);
    }

    @Override
    public void resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        String cleanEmail = email.trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + cleanEmail));

        Customer customer = user.getCustomer() != null
                ? user.getCustomer()
                : customerRepository.findByEmail(cleanEmail).orElse(null);

        if (Boolean.TRUE.equals(user.getIsEmailVerified()) || (customer != null && Boolean.TRUE.equals(customer.getIsEmailVerified()))) {
            throw new RuntimeException("Email is already verified.");
        }

        String newVerificationToken = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(24, ChronoUnit.HOURS);

        user.setVerificationToken(newVerificationToken);
        user.setVerificationTokenExpiry(expiry);
        userRepository.save(user);

        if (customer != null) {
            customer.setVerificationToken(newVerificationToken);
            customer.setVerificationTokenExpiry(expiry);
            customerRepository.save(customer);
        }

        String customerName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");
        emailService.sendRegistrationSuccessEmail(
                user.getEmail(),
                customerName,
                "********",
                newVerificationToken
        );

        log.info("New verification email sent to: {}", cleanEmail);
    }

}
