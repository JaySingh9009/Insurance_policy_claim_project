package com.insurance.demo.controller;

import com.insurance.demo.dto.*;
import com.insurance.demo.security.CustomUserDetails;
import com.insurance.demo.service.ClaimService;
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
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claim submission and lifecycle management")
public class ClaimController {

    private final ClaimService claimService;

    // ─── Customer: Submit Claim ───────────────────────────────────────────────

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @Operation(summary = "Submit a new claim (Customer only)")
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.submitClaim(request, principal.getUser().getId()));
    }

    // ─── Customer: My Claims ──────────────────────────────────────────────────

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    @Operation(summary = "Get my claims (Customer only)")
    public ResponseEntity<PagedResponse<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(claimService.getMyClaims(
                principal.getUser().getId(), page, size, sortBy, sortDir));
    }

    // ─── Agent: Review / Recommend ────────────────────────────────────────────

    @PreAuthorize("hasRole('AGENT')")
    @PatchMapping("/{id}/review")
    @Operation(summary = "Agent moves claim to UNDER_REVIEW")
    public ResponseEntity<ClaimResponse> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody AgentRemarkRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        // Force targetStatus to UNDER_REVIEW for this endpoint
        request.setTargetStatus("UNDER_REVIEW");
        return ResponseEntity.ok(claimService.updateClaimStatus(id, request, principal.getUser().getId()));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PatchMapping("/{id}/recommend")
    @Operation(summary = "Agent recommends APPROVAL or REJECTION")
    public ResponseEntity<ClaimResponse> recommendClaim(
            @PathVariable Long id,
            @Valid @RequestBody AgentRemarkRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(claimService.updateClaimStatus(id, request, principal.getUser().getId()));
    }

    // ─── Admin: Final Decision ────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/decision")
    @Operation(summary = "Admin makes final APPROVED or REJECTED decision")
    public ResponseEntity<ClaimResponse> makeDecision(
            @PathVariable Long id,
            @Valid @RequestBody ClaimDecisionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(claimService.makeClaimDecision(id, request, principal.getUser().getId()));
    }

    // ─── Admin/Agent: All Claims ──────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @GetMapping
    @Operation(summary = "Get all claims (Admin/Agent)")
    public ResponseEntity<PagedResponse<ClaimResponse>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(claimService.getAllClaims(page, size, sortBy, sortDir));
    }

    // ─── Get Claim by ID ──────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    // ─── Claim History ────────────────────────────────────────────────────────

    @GetMapping("/{id}/history")
    @Operation(summary = "Get claim status history (Admin/Agent/Customer for own claim)")
    public ResponseEntity<PagedResponse<ClaimHistoryResponse>> getClaimHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(claimService.getClaimHistory(id, page, size));
    }
}