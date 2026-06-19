package com.insurance.demo.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.entity.OtpVerification;
import com.insurance.demo.entity.User;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.repository.OtpVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final SmsService smsService;          // NEW: injected SMS service
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    /**
     * Generates separate OTPs for email and phone, persists them, and dispatches both.
     */
    @Transactional
    public void createAndSendOtp(User user) {
        String emailOtp = generateSixDigitOtp();
        String phoneOtp = generateSixDigitOtp();   // NEW: separate phone OTP

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .emailOtp(emailOtp)
                .phoneOtp(phoneOtp)                // NEW: stored in entity
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        otpRepository.save(otpVerification);

        // Send email OTP
        emailService.sendOtp(user.getEmail(), emailOtp);

        // NEW: Send phone OTP via Twilio SMS
        // mobileNumber stored as 10-digit; prefix with country code for Twilio
        String formattedPhone = formatToE164(user.getMobileNumber());
        smsService.sendOtp(formattedPhone, phoneOtp);

        log.info("Email OTP and SMS OTP created and dispatched for userId={}", user.getId());
    }

    /**
     * Validates that EITHER the email OTP or the phone OTP matches
     * (user chose one channel during verify-otp step).
     * Marks OTP record as used on success.
     */
    @Transactional
    public void verifyOtp(User user, String providedOtp, String channel) {
        OtpVerification latestOtp = otpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException(
                        "No active OTP found. Please register again to receive a new OTP."));

        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please register again to receive a new OTP.");
        }

        if ("email".equalsIgnoreCase(channel)) {
            if (!latestOtp.getEmailOtp().equals(providedOtp)) {
                throw new BadRequestException("Invalid email OTP. Please check your email and try again.");
            }
        } else if ("phone".equalsIgnoreCase(channel)) {
            if (!latestOtp.getPhoneOtp().equals(providedOtp)) {
                throw new BadRequestException("Invalid phone OTP. Please check your SMS and try again.");
            }
        } else {
            throw new BadRequestException("Invalid channel. Use 'email' or 'phone'.");
        }

        latestOtp.setUsed(true);
        otpRepository.save(latestOtp);
        log.info("OTP verified successfully via channel={} for userId={}", channel, user.getId());
    }

    private String generateSixDigitOtp() {
        int number = secureRandom.nextInt(900000) + 100000; // always 6 digits: 100000–999999
        return String.valueOf(number);
    }

    /**
     * Converts a 10-digit Indian mobile number to E.164 format for Twilio.
     * Adjust the country code prefix if your users are in a different region.
     */
    private String formatToE164(String mobileNumber) {
        if (mobileNumber == null) return "";
        String digits = mobileNumber.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return "+91" + digits;   // India country code; change as needed
        }
        // Already has country code or some other format — return as-is with + prefix
        return "+" + digits;
    }
}
