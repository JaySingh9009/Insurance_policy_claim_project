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

    
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    @Operation(summary = "Submit a new claim (Customer only)")
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.submitClaim(request, principal.getUser().getId()));
    }

    
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

    
    @PreAuthorize("hasRole('AGENT')")
    @RequestMapping(value = "/{id}/review", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Agent moves claim to UNDER_REVIEW")
    public ResponseEntity<ClaimResponse> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody AgentRemarkRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
                request.setTargetStatus("UNDER_REVIEW");
        return ResponseEntity.ok(claimService.updateClaimStatus(id, request, principal.getUser().getId()));
    }

    @PreAuthorize("hasRole('AGENT')")
    @RequestMapping(value = "/{id}/recommend", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Agent recommends APPROVAL or REJECTION")
    public ResponseEntity<ClaimResponse> recommendClaim(
            @PathVariable Long id,
            @Valid @RequestBody AgentRemarkRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(claimService.updateClaimStatus(id, request, principal.getUser().getId()));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = {"/{id}/decide"}, method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Admin makes final APPROVED or REJECTED decision")
    public ResponseEntity<ClaimResponse> makeDecision(
            @PathVariable Long id,
            @Valid @RequestBody ClaimDecisionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(claimService.makeClaimDecision(id, request, principal.getUser().getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = {"/{id}/assign", "/{id}/assign-agent"}, method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Admin assigns an agent to a claim (Admin only)")
    public ResponseEntity<ClaimResponse> assignAgent(
            @PathVariable Long id,
            @RequestParam(required = false) Long agentId,
            @RequestBody(required = false) AssignAgentRequest request) {
        Long finalAgentId = (agentId != null) ? agentId : (request != null ? request.getAgentId() : null);
        if (finalAgentId == null) {
            throw new com.insurance.demo.exception.BadRequestException("Agent ID must be provided.");
        }
        return ResponseEntity.ok(claimService.assignAgent(id, finalAgentId));
    }


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





}