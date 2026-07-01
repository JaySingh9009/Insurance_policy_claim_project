package com.insurance.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Existing method: Registration OTP ──────────────────────────────────
    public void sendOtp(String toEmail, String otp) {
        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalStateException("Email service is not configured. Please set spring.mail.username.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail.trim());
            message.setTo(toEmail);
            message.setSubject("Insurance Portal - Email Verification OTP");
            message.setText(
                    "Dear User,\n\n" +
                    "Your email verification OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 5 minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Regards,\n" +
                    "Insurance Policy & Claim Management Team"
            );

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MailException ex) {
            Throwable rootCause = ex;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            log.error("Failed to send OTP email to {}. Root cause: {} - {}",
                    toEmail, rootCause.getClass().getSimpleName(), rootCause.getMessage());
            throw new IllegalStateException(
                    "Unable to send OTP email. Root cause: " +
                    rootCause.getClass().getSimpleName() + " - " + rootCause.getMessage(), ex);
        }
    }

    // ── NEW method: Forgot Password OTP ────────────────────────────────────
    public void sendPasswordResetOtp(String toEmail, String otp) {
        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalStateException("Email service is not configured. Please set spring.mail.username.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail.trim());
            message.setTo(toEmail);
            message.setSubject("Insurance Portal - Password Reset OTP");
            message.setText(
                    "Dear User,\n\n" +
                    "We received a request to reset your password.\n\n" +
                    "Your password reset OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 5 minutes.\n\n" +
                    "If you did NOT request a password reset, please ignore this email. " +
                    "Your password will remain unchanged.\n\n" +
                    "Regards,\n" +
                    "Insurance Policy & Claim Management Team"
            );

            mailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", toEmail);

        } catch (MailException ex) {
            Throwable rootCause = ex;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            log.error("Failed to send password reset OTP to {}. Root cause: {} - {}",
                    toEmail, rootCause.getClass().getSimpleName(), rootCause.getMessage());
            throw new IllegalStateException(
                    "Unable to send password reset OTP. Root cause: " +
                    rootCause.getClass().getSimpleName() + " - " + rootCause.getMessage(), ex);
        }
    }
}