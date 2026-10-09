package com.ampta.utils;

import com.ampta.entity.enums.DocumentStatus;

public interface EmailService {
    void sendSimpleEmail(String toEmail, String subject, String body);
    void sendHtmlEmail(String toEmail, String subject, String htmlBody);
    void sendUserCredentials(String toEmail, String name, String password);
    void sendEmployeeCreatedNotification(String toEmail, String name, String department, Double salary);
    void send2FaRecoveryOtp(String toEmail, String name, String otp);
    void sendPasswordResetEmail(String toEmail, String name, String resetToken);
    void sendRegistrationSuccessEmail(String toEmail, String name, String password, String verificationToken);
    void sendKycStatusEmail(
            String toEmail,
            String name,
            String documentType,
            DocumentStatus status,
            String rejectionReason
    );
}