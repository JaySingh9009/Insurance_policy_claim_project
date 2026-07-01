package com.insurance.demo.controller;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.security.CustomUserDetails;
import com.insurance.demo.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Premium Payments", description = "Record and view premium payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Make a premium payment (Customer/Agent/Admin)")
    public ResponseEntity<PaymentResponse> makePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String role = principal.getUser().getRole().name();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.makePayment(request, principal.getUser().getId(), role));
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get payments for a policy")
    public ResponseEntity<PagedResponse<PaymentResponse>> getPaymentsByPolicy(
            @PathVariable Long policyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsByPolicy(policyId, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @GetMapping
    @Operation(summary = "Get all payments (Admin/Agent)")
    public ResponseEntity<PagedResponse<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, size, sortBy, sortDir));
    }
    
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(
                        principal.getUser().getId(),
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }
}