package com.insurance.demo.controller;

import com.insurance.demo.dto.IssuePolicyRequest;
import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PolicyResponse;
import com.insurance.demo.dto.PurchasePolicyRequest;
import com.insurance.demo.security.CustomUserDetails;
import com.insurance.demo.service.PolicyService;
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
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Policy purchase, issuance, and management")
public class PolicyController {

    private final PolicyService policyService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/purchase")
    @Operation(summary = "Customer purchases a policy (Customer only)")
    public ResponseEntity<PolicyResponse> purchasePolicy(
            @Valid @RequestBody PurchasePolicyRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyService.purchasePolicy(request, principal.getUser().getId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @PostMapping("/issue")
    @Operation(summary = "Admin/Agent issues policy to a specific customer")
    public ResponseEntity<PolicyResponse> issuePolicy(@Valid @RequestBody IssuePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.issuePolicy(request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my policies (Customer only)")
    public ResponseEntity<PagedResponse<PolicyResponse>> getMyPolicies(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(policyService.getMyPolicies(
                principal.getUser().getId(), page, size, sortBy, sortDir));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @GetMapping
    @Operation(summary = "Get all policies (Admin/Agent)")
    public ResponseEntity<PagedResponse<PolicyResponse>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(policyService.getAllPolicies(page, size, sortBy, sortDir));
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a policy (Admin/Agent only)")
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        String role = principal.getUser().getRole().name();
        return ResponseEntity.ok(policyService.cancelPolicy(id, principal.getUser().getId(), role));
    }
}