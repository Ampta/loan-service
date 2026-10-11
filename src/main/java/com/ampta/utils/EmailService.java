package com.ampta.utils;

import com.ampta.entity.enums.DocumentStatus;

import java.util.List;

public interface EmailService {
    void sendSimpleEmail(String toEmail, String subject, String body);
    void sendHtmlEmail(String toEmail, String subject, String htmlBody);
    void sendUserCredentials(String toEmail, String name, String password);
    void sendEmployeeCreatedNotification(String toEmail, String name, String department, Double salary);
    void send2FaRecoveryOtp(String toEmail, String name, String otp);
    void sendPasswordResetEmail(String toEmail, String name, String resetToken);
    void sendLoanApprovedEmail(
            String toEmail,
            String name,
            Long dealId,
            Double approvedAmount
    );

    public void sendLoanDisbursementEmail(
            String toEmail,
            String name,
            Long dealId,
            Double disbursedAmount,
            String loanAccountNo,
            String bankPartner,
            String disbursementDate
    );
    void sendRegistrationSuccessEmail(String toEmail, String name, String password, String verificationToken);

    void sendBulkKycStatusEmail(
            String toEmail,
            String name,
            List<String> documentTypes,
            DocumentStatus status,
            String rejectionReason
    );

}