package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.LoginRequest;
import com.insurance.demo.dto.LoginResponse;
import com.insurance.demo.dto.RegisterRequest;
import com.insurance.demo.dto.UserResponse;
import com.insurance.demo.dto.VerifyOtpRequest;
import com.insurance.demo.entity.User;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateEmailException;
import com.insurance.demo.exception.InactiveUserException;
import com.insurance.demo.exception.InvalidCredentialsException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.UserRepository;
import com.insurance.demo.security.JwtService;
import com.insurance.demo.service.AuthService;
import com.insurance.demo.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Save user as INACTIVE — account activates only after OTP verification
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNumber(request.getMobileNumber())
                .role(Role.CUSTOMER)
                .active(false)   // <-- inactive until OTP verified
                .build();

        user = userRepository.save(user);
        log.info("User saved (inactive), sending OTP via email and SMS: userId={}, email={}", user.getId(), user.getEmail());

        // NEW: sends both an email OTP and a phone/SMS OTP simultaneously
        otpService.createAndSendOtp(user);

        return "Registration successful. An OTP has been sent to both your email (" + user.getEmail() +
               ") and your registered mobile number. Please verify using either your email OTP or phone OTP to activate your account.";
    }

    @Override
    @Transactional
    public UserResponse verifyOtp(VerifyOtpRequest request) {
        log.info("OTP verification attempt for email: {} via channel: {}", request.getEmail(), request.getChannel());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (user.isActive()) {
            throw new BadRequestException("Account is already verified and active.");
        }

        // NEW: pass channel ("email" or "phone") so OtpService validates the right OTP
        otpService.verifyOtp(user, request.getOtp(), request.getChannel());

        // Activate account
        user.setActive(true);
        userRepository.save(user);
        log.info("OTP verified successfully via channel={} — account activated: userId={}", request.getChannel(), user.getId());

        return mapToUserResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!user.isActive()) {
            log.warn("Login attempt by inactive/unverified user: {}", request.getEmail());
            throw new InactiveUserException(
                    "Your account is not yet verified. Please check your email or SMS for the OTP and verify your account.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - incorrect password for: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        log.info("Login successful: userId={}, role={}", user.getId(), user.getRole());

        return new LoginResponse(token, user.getEmail(), user.getRole().name());
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.isActive()
        );
    }
}
