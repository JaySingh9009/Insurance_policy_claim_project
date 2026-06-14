package com.insurance.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.LoginRequest;
import com.insurance.demo.dto.LoginResponse;
import com.insurance.demo.dto.RegisterRequest;
import com.insurance.demo.dto.UserResponse;
import com.insurance.demo.dto.VerifyOtpRequest;
import com.insurance.demo.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Public registration, OTP verification and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new customer account",
        description = "Creates an inactive account and sends a 6-digit OTP to the registered email."
    )
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP to activate account",
        description = "Validates the OTP sent to the user's email. Activates the account on success."
    )
    public ResponseEntity<UserResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login and receive JWT token",
        description = "Only verified (active) accounts can log in."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
