package com.insurance.demo.service;

import com.insurance.demo.entity.OtpVerification;
import com.insurance.demo.entity.User;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:5}")
    private long expiryMinutes;

    /**
     * Generates a 6-digit OTP, persists it, and sends it to the user's email.
     */
    @Transactional
    public void createAndSendOtp(User user) {
        String otp = generateSixDigitOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .emailOtp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        otpRepository.save(otpVerification);
        emailService.sendOtp(user.getEmail(), otp);
        log.info("OTP created and sent for userId={}", user.getId());
    }

    /**
     * Validates the OTP provided by the user.
     * Marks it as used on success.
     */
    @Transactional
    public void verifyOtp(User user, String providedOtp) {
        OtpVerification latestOtp = otpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException(
                        "No active OTP found. Please register again to receive a new OTP."));

        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please register again to receive a new OTP.");
        }

        if (!latestOtp.getEmailOtp().equals(providedOtp)) {
            throw new BadRequestException("Invalid OTP. Please check your email and try again.");
        }

        latestOtp.setUsed(true);
        otpRepository.save(latestOtp);
        log.info("OTP verified successfully for userId={}", user.getId());
    }

    private String generateSixDigitOtp() {
        int number = secureRandom.nextInt(900000) + 100000; // always 6 digits: 100000–999999
        return String.valueOf(number);
    }
}
