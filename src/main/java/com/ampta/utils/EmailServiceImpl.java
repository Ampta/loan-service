package com.ampta.utils;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
}

