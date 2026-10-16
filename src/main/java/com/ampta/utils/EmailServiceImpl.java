package com.ampta.utils;

import com.ampta.entity.enums.DocumentStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private String getCleanBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Simple email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendUserCredentials(String toEmail, String name, String password) {
        String subject = "Welcome to Ampta Loan Service - Your Account Credentials";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <meta name="supported-color-schemes" content="light">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 32px 28px; text-align: left;">
                                    <tr>
                                        <td>
                                            <h2 style="margin: 0 0 12px 0; color: #2563eb; font-size: 22px; font-weight: 700;">Ampta Loan Service</h2>
                                            <h3 style="margin: 0 0 16px 0; color: #0f172a; font-size: 18px; font-weight: 600;">Welcome, %s!</h3>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">Your account has been created successfully. Below are your login credentials:</p>
                                            
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 14px 16px; margin: 20px 0;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 4px 0; font-size: 14px; color: #334155;"><strong>Email:</strong> %s</p>
                                                        <p style="margin: 4px 0; font-size: 14px; color: #334155;"><strong>Temporary Password:</strong> <code style="background-color: #ffffff; border: 1px solid #e2e8f0; padding: 2px 8px; border-radius: 4px; font-weight: 600; color: #0f172a;">%s</code></p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 0 0 16px 0; font-size: 13px; color: #e11d48; font-weight: 500;">For your security, please log in and change your password immediately.</p>
                                            
                                            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0 16px 0;" />
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; text-align: center;">Ampta Loan Service • Automated Notification</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(name != null ? name : "User", toEmail, password);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void sendEmployeeCreatedNotification(String toEmail, String name, String department, Double salary) {
        String subject = "Welcome to Ampta Loan Service - Employee Profile Created";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <meta name="supported-color-schemes" content="light">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 32px 28px; text-align: left;">
                                    <tr>
                                        <td>
                                            <h2 style="margin: 0 0 12px 0; color: #2563eb; font-size: 22px; font-weight: 700;">Ampta Loan Service</h2>
                                            <h3 style="margin: 0 0 16px 0; color: #0f172a; font-size: 18px; font-weight: 600;">Welcome, %s!</h3>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">Your employee profile has been created successfully with the following details:</p>
                                            
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 14px 16px; margin: 20px 0;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 4px 0; font-size: 14px; color: #334155;"><strong>Department:</strong> %s</p>
                                                        <p style="margin: 4px 0; font-size: 14px; color: #334155;"><strong>Salary:</strong> ₹%.2f</p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0 16px 0;" />
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; text-align: center;">Ampta Loan Service HR Team</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(name != null ? name : "Employee", department != null ? department : "General", salary != null ? salary : 0.0);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void send2FaRecoveryOtp(String toEmail, String name, String otp) {
        String subject = "Ampta Loan Service - Two-Factor Authentication Recovery Code";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <meta name="supported-color-schemes" content="light">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 32px 28px; text-align: left;">
                                    <tr>
                                        <td>
                                            <h2 style="margin: 0 0 12px 0; color: #2563eb; font-size: 22px; font-weight: 700;">Ampta Loan Service</h2>
                                            <h3 style="margin: 0 0 16px 0; color: #0f172a; font-size: 18px; font-weight: 600;">2FA Recovery Code</h3>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">Hello, %s!</p>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">You requested to reset your Two-Factor Authentication (2FA). Use the 6-digit confirmation code below to verify your identity:</p>
                                            
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center;">
                                                <tr>
                                                    <td align="center">
                                                        <span style="font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #1d4ed8; font-family: monospace;">%s</span>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 0 0 16px 0; font-size: 12px; color: #64748b;">This code is valid for 15 minutes. If you did not request this, please secure your account immediately.</p>
                                            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0 16px 0;" />
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; text-align: center;">Ampta Loan Service Security Team • Automated Notification</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(name != null ? name : toEmail, otp);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        String subject = "Ampta Loan Service - Password Reset Request";
        String resetLink = getCleanBaseUrl() + "/api/v1/auth/reset-password?email=" + toEmail + "&token=" + resetToken;

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <meta name="supported-color-schemes" content="light">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 32px 28px; text-align: left;">
                                    <tr>
                                        <td>
                                            <h2 style="margin: 0 0 12px 0; color: #2563eb; font-size: 22px; font-weight: 700;">Ampta Loan Service</h2>
                                            <h3 style="margin: 0 0 16px 0; color: #0f172a; font-size: 18px; font-weight: 600;">Password Reset Request</h3>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">Hello, %s!</p>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">We received a request to reset your password. Click the button below or enter the reset token manually:</p>
                                            
                                            <div style="text-align: center; margin: 24px 0;">
                                                <a href="%s" style="background-color: #2563eb; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 15px; display: inline-block;">Reset Password</a>
                                            </div>

                                            <p style="margin: 16px 0 6px 0; font-size: 13px; color: #334155;"><strong>Or use your reset token manually:</strong></p>
                                            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 12px 14px; border-radius: 6px; word-break: break-all; margin-bottom: 20px;">
                                                <span style="font-family: monospace; font-size: 14px; font-weight: 600; color: #0f172a;">%s</span>
                                            </div>
                                            
                                            <p style="margin: 0 0 16px 0; font-size: 12px; color: #64748b;">This token is valid for 15 minutes. If you did not request a password reset, you can safely ignore this email.</p>
                                            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0 16px 0;" />
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; text-align: center;">Ampta Loan Service Security Team • Automated Notification</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(name != null ? name : toEmail, resetLink, resetToken);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void sendRegistrationSuccessEmail(String toEmail, String name, String password, String verificationToken) {
        String subject = "Welcome to Ampta Loan Service - Account Verification";
        String verifyUrl = getCleanBaseUrl() + "/api/v1/auth/verify-email?token=" + verificationToken;

        String passwordRow = "";
        if (password != null && !password.isBlank() && !"********".equals(password)) {
            passwordRow = "<p style=\"margin: 4px 0; font-size: 14px; color: #334155;\"><strong>Initial Password:</strong> <code style=\"background-color: #ffffff; border: 1px solid #e2e8f0; padding: 2px 8px; border-radius: 4px; font-weight: 600; color: #0f172a;\">" + password + "</code></p>";
        }

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light">
                    <meta name="supported-color-schemes" content="light">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 32px 28px; text-align: left;">
                                    <tr>
                                        <td>
                                            <h2 style="margin: 0 0 12px 0; color: #2563eb; font-size: 22px; font-weight: 700;">Ampta Loan Service</h2>
                                            <h3 style="margin: 0 0 16px 0; color: #0f172a; font-size: 18px; font-weight: 600;">Welcome, %s!</h3>
                                            <p style="margin: 0 0 16px 0; color: #334155; font-size: 14px;">Thank you for registering with Ampta Loan Service. Your account has been registered successfully. Please verify your email address to activate your account:</p>
                                            
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 14px 16px; margin: 20px 0;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 4px 0; font-size: 14px; color: #334155;"><strong>Registered Email:</strong> %s</p>
                                                        %s
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <div style="text-align: center; margin: 24px 0;">
                                                <a href="%s" style="background-color: #2563eb; color: #ffffff !important; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 15px; display: inline-block;">Verify Email Address</a>
                                            </div>

                                            <p style="margin: 16px 0 6px 0; font-size: 12px; color: #64748b;">Or copy and paste this verification URL into your browser:</p>
                                            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; padding: 10px 12px; border-radius: 6px; word-break: break-all; margin-bottom: 20px;">
                                                <a href="%s" style="color: #2563eb; font-size: 12px; text-decoration: none;">%s</a>
                                            </div>

                                            <p style="margin: 0 0 16px 0; font-size: 12px; color: #64748b;">This verification link is valid for 24 hours. If you did not create an account with Ampta, you can safely ignore this email.</p>
                                            
                                            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0 16px 0;" />
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8; text-align: center;">Ampta Loan Service • Automated System Notification</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                name != null && !name.isBlank() ? name : "Customer",
                toEmail,
                passwordRow,
                verifyUrl,
                verifyUrl,
                verifyUrl
        );

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void sendBulkKycStatusEmail(
            String toEmail,
            String name,
            List<String> documentTypes,
            DocumentStatus status,
            String rejectionReason) {

        if (status != DocumentStatus.APPROVED
                && status != DocumentStatus.REJECTED) {
            log.warn("Invalid KYC email status: {}", status);
            return;
        }

        String customerName =
                (name == null || name.isBlank()) ? "Customer" : name;

        String documents = String.join(", ", documentTypes);

        String subject;
        String body;

        if (status == DocumentStatus.APPROVED) {
            subject = "KYC Documents Approved - Ampta Loan Service";
            body = "Hello " + customerName + ",\n\n"
                    + "Your selected KYC documents have been approved.\n\n"
                    + "Documents: " + documents + "\n"
                    + "Status: APPROVED\n\n"
                    + "No further action is required for these documents.\n\n"
                    + "Regards,\n"
                    + "Ampta Loan Service Team";
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                log.warn("Cannot send KYC rejection email without a reason");
                return;
            }

            subject = "KYC Documents Rejected - Ampta Loan Service";
            body = "Hello " + customerName + ",\n\n"
                    + "Your selected KYC documents have been rejected.\n\n"
                    + "Documents: " + documents + "\n"
                    + "Status: REJECTED\n"
                    + "Reason: " + rejectionReason.trim() + "\n\n"
                    + "Please review the reason and upload corrected documents.\n\n"
                    + "Regards,\n"
                    + "Ampta Loan Service Team";
        }

        sendSimpleEmail(toEmail, subject, body);
    }

    @Override
    public void sendEligibilityApprovedEmail(
            String toEmail,
            String name,
            Integer cibilScore,
            Double loanAmount,
            boolean automatic) {

        String customerName =
                (name == null || name.isBlank()) ? "Customer" : name;

        String subject = automatic
                ? "Loan Eligibility Automatically Approved"
                : "Loan Eligibility Approved";

        String amount = loanAmount == null
                ? "To be determined"
                : "INR " + String.format(
                java.util.Locale.US,
                "%.2f",
                loanAmount
        );

        String body =
                "Dear " + customerName + ",\n\n"
                        + "Congratulations! Your loan eligibility has been "
                        + (automatic ? "automatically approved" : "approved by our officer")
                        + ".\n\n"
                        + "CIBIL Score: " + cibilScore + "\n"
                        + "Indicative Loan Amount: " + amount + "\n\n"
                        + "Please note that eligibility approval does not necessarily "
                        + "mean final loan sanction or disbursement.\n\n"
                        + "Regards,\n"
                        + "LoanApp Team";

        sendSimpleEmail(toEmail, subject, body);
    }

    @Override
    public void sendEligibilityRejectedEmail(
            String toEmail,
            String name,
            Integer cibilScore,
            String rejectionReason) {

        String customerName =
                (name == null || name.isBlank()) ? "Customer" : name;

        String subject = "Loan Eligibility Rejected";

        String body =
                "Dear " + customerName + ",\n\n"
                        + "We regret to inform you that your loan eligibility "
                        + "request has been rejected by our loan officer.\n\n"
                        + "CIBIL Score: " + cibilScore + "\n"
                        + "Rejection Reason: " + rejectionReason + "\n\n"
                        + "For further assistance, please contact our loan team.\n\n"
                        + "Regards,\n"
                        + "LoanApp Team";

        sendSimpleEmail(toEmail, subject, body);
    }

    @Override
    @Async
    public void sendLoanApprovedEmail(
            String toEmail,
            String name,
            Long dealId,
            Double approvedAmount
    ) {
        String subject = "Loan Approved - Next Process Pending";

        String customerName = name != null ? name : "Customer";

        String amountText = approvedAmount != null
                ? String.format("%.2f", approvedAmount)
                : "Not available";

        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;
                            border: 1px solid #e0e0e0; border-radius: 8px;">

                    <h2 style="color: #2f855a;">
                        Loan Approved Successfully
                    </h2>

                    <p>Hello, %s!</p>

                    <p>
                        We are pleased to inform you that your loan application
                        has been <strong>approved</strong>.
                    </p>

                    <div style="background-color: #f0fff4; padding: 15px;
                                border-radius: 6px; margin: 20px 0;">

                        <p><strong>Loan Deal ID:</strong> %s</p>
                        <p><strong>Approved Amount:</strong> ₹%s</p>
                        <p><strong>Loan Status:</strong>
                            <span style="color: #2f855a; font-weight: bold;">
                                APPROVED
                            </span>
                        </p>
                        <p><strong>Next Process:</strong>
                            <span style="color: #d69e2e; font-weight: bold;">
                                PENDING
                            </span>
                        </p>
                    </div>

                    <p>
                        The next process is currently pending and will be
                        initiated by our team.
                    </p>

                    <p>
                        You will receive another notification when the next
                        process is completed or requires your action.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e0e0e0;
                               margin: 20px 0;" />

                    <p style="font-size: 0.8em; color: #718096;">
                        This is an automated message. Please do not reply directly.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(customerName, dealId, amountText);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }


    @Override
    @Async
    public void sendLoanDisbursementEmail(
            String toEmail,
            String name,
            Long dealId,
            Double disbursedAmount,
            String loanAccountNo,
            String bankPartner,
            String disbursementDate
    ) {

        String subject = "Loan Amount Disbursed Successfully";

        String customerName = name != null ? name : "Customer";

        String amountText = disbursedAmount != null
                ? String.format(java.util.Locale.US, "%.2f", disbursedAmount)
                : "Not available";

        String accountNumber = loanAccountNo != null
                ? loanAccountNo
                : "Not available";

        String bankName = bankPartner != null
                ? bankPartner
                : "Not available";

        String dateText = disbursementDate != null
                ? disbursementDate
                : "Not available";

        String htmlBody = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;
                        border: 1px solid #e0e0e0; border-radius: 8px;">

                <h2 style="color: #2f855a;">
                    Loan Amount Disbursed Successfully
                </h2>

                <p>Hello, %s!</p>

                <p>
                    We are pleased to inform you that your loan amount
                    has been <strong>disbursed successfully</strong>
                    and your loan account has been created.
                </p>

                <div style="background-color: #f0fff4; padding: 15px;
                            border-radius: 6px; margin: 20px 0;">

                    <p><strong>Loan Deal ID:</strong> %s</p>
                    <p><strong>Loan Account Number:</strong> %s</p>
                    <p><strong>Disbursed Amount:</strong> ₹%s</p>
                    <p><strong>Bank Partner:</strong> %s</p>
                    <p><strong>Disbursement Date:</strong> %s</p>

                    <p>
                        <strong>Loan Status:</strong>
                        <span style="color: #2f855a; font-weight: bold;">
                            DISBURSED
                        </span>
                    </p>
                </div>

                <p>
                    Please keep your loan account number for future
                    reference. Your repayment obligations will be
                    governed by your loan agreement and repayment schedule.
                </p>

                <p>
                    If you have any questions, please contact our
                    loan management team.
                </p>

                <hr style="border: none; border-top: 1px solid #e0e0e0;
                           margin: 20px 0;" />

                <p style="font-size: 0.8em; color: #718096;">
                    This is an automated message. Please do not reply directly.
                </p>
            </div>
        </body>
        </html>
        """.formatted(
                escapeHtml(customerName),
                dealId,
                escapeHtml(accountNumber),
                amountText,
                escapeHtml(bankName),
                escapeHtml(dateText)
        );

        sendHtmlEmail(toEmail, subject, htmlBody);
    }


    private String escapeHtml(String value) {
        if (value == null) {
            return "Not available";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }


    @Override
    @Async
    public void sendSanctionLetterEmail(
            String toEmail,
            String customerName,
            Long dealId,
            String pdfPath) {

        try {
            if (toEmail == null || toEmail.isBlank()) {
                throw new IllegalArgumentException(
                        "Customer email is required"
                );
            }

            File pdfFile = new File(pdfPath);

            if (!pdfFile.exists() || !pdfFile.isFile()) {
                throw new IllegalArgumentException(
                        "Sanction letter PDF not found: " + pdfPath
                );
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // true enables email attachments
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String name = (customerName == null || customerName.isBlank())
                    ? "Customer"
                    : customerName;

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "Loan Sanction Letter - Deal ID: " + dealId
            );

            String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;
                             line-height: 1.6; color: #333;">

                    <div style="max-width: 600px; margin: auto;
                                padding: 20px; border: 1px solid #ddd;
                                border-radius: 8px;">

                        <h2 style="color: #174a7e;">
                            Loan Sanction Letter
                        </h2>

                        <p>Hello %s,</p>

                        <p>
                            Congratulations! Your loan sanction letter
                            has been generated successfully.
                        </p>

                        <p>
                            <strong>Loan Deal ID:</strong> %s
                        </p>

                        <p>
                            Please find your sanction letter attached
                            to this email as a PDF document.
                        </p>

                        <p>
                            Please keep this document for your records
                            and review the terms and conditions carefully.
                        </p>

                        <p>Regards,<br/>
                        Loan Management Team</p>

                        <hr/>

                        <small>
                            This is an automated email. Please do not
                            reply directly.
                        </small>

                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(name), dealId);

            helper.setText(htmlBody, true);

            FileSystemResource attachment =
                    new FileSystemResource(pdfFile);

            helper.addAttachment(
                    pdfFile.getName(),
                    attachment,
                    "application/pdf"
            );

            mailSender.send(mimeMessage);

            log.info(
                    "Sanction letter email sent to {} for deal ID {}",
                    toEmail,
                    dealId
            );

        } catch (Exception e) {
            log.error(
                    "Failed to send sanction letter email to {} for deal ID {}",
                    toEmail,
                    dealId,
                    e
            );
        }
    }










}


