package com.ampta.utils;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


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
        }catch (Exception e){
            log.error("failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to {}", toEmail);
        }catch (Exception e){
            log.error("failed to send HTML email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendUserCredentials(String toEmail, String name, String password) {
        String subject = "Welcome to Our Platform - Your Account Credentials";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #2b6cb0;">Welcome, %s!</h2>
                        <p>Your account has been created successfully. Below are your login credentials:</p>
                        
                        <div style="background-color: #f7fafc; padding: 15px; border-radius: 6px; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Email:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Temporary Password:</strong> <code style="background: #edf2f7; padding: 2px 6px; border-radius: 4px;">%s</code></p>
                        </div>
                        
                        <p style="color: #e53e3e; font-size: 0.9em;">Please change your password after logging in for security.</p>
                        
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;" />
                        <p style="font-size: 0.8em; color: #718096;">If you did not request this account, please contact support.</p>
                    </div>
                </body>
                </html>
                """.formatted(name, toEmail, password);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    public void sendEmployeeCreatedNotification(String toEmail, String name, String department, Double salary) {

    }

    @Override
    @Async
    public void send2FaRecoveryOtp(String toEmail, String name, String otp) {
        String subject = "Two-Factor Authentication Recovery Code";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #c53030;">Two-Factor Authentication Recovery</h2>
                        <p>Hello, %s!</p>
                        <p>You have requested to reset your Two-Factor Authentication (2FA). Use the 6-digit confirmation code below:</p>
                        
                        <div style="background-color: #fff5f5; border-left: 4px solid #e53e3e; padding: 15px; margin: 20px 0;">
                            <span style="font-size: 28px; font-weight: bold; letter-spacing: 6px; color: #c53030;">%s</span>
                        </div>
                        
                        <p style="color: #718096; font-size: 0.9em;">This code is valid for 15 minutes. If you did not request this, please secure your account immediately.</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;" />
                        <p style="font-size: 0.8em; color: #a0aec0;">Automated message from Security Team.</p>
                    </div>
                </body>
                </html>
                """.formatted(name != null ? name : toEmail, otp);

        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:8080/reset-password?email=" + toEmail + "&token=" + resetToken;

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #2b6cb0;">Password Reset Request</h2>
                        <p>Hello, %s!</p>
                        <p>We received a request to reset your password. Click the button below or use your reset token to set a new password:</p>
                        
                        <div style="text-align: center; margin: 25px 0;">
                            <a href="%s" style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Reset My Password</a>
                        </div>

                        <p style="margin-bottom: 5px;"><strong>Or enter the reset token manually:</strong></p>
                        <div style="background-color: #edf2f7; padding: 12px 15px; border-radius: 6px; margin: 10px 0; word-break: break-all;">
                            <code style="font-size: 15px; font-weight: bold; color: #1e293b;">%s</code>
                        </div>
                        
                        <p style="color: #718096; font-size: 0.9em;">This token is valid for 15 minutes. If you did not request a password reset, you can safely ignore this email.</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;" />
                        <p style="font-size: 0.8em; color: #a0aec0;">Automated message from Security Team.</p>
                    </div>
                </body>
                </html>
                """.formatted(name != null ? name : toEmail, resetLink, resetToken);

        sendHtmlEmail(toEmail, subject, htmlBody);
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




