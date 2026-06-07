package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.ClaimRequest;
import com.insurance.demo.dto.ClaimResponse;
import com.insurance.demo.dto.ClaimReviewRequest;
import com.insurance.demo.service.ClaimService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ClaimResponse>
    submitClaim(
            @RequestBody ClaimRequest request){

        return ResponseEntity.ok(
                claimService
                .submitClaim(request));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<List<ClaimResponse>>
    getClaims(
            @PathVariable Long policyId){

        return ResponseEntity.ok(
                claimService
                .getClaims(policyId));
    }
    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{claimId}/review")
    public ResponseEntity<ClaimResponse>
    reviewClaim(
            @PathVariable Long claimId,
            @RequestBody ClaimReviewRequest request){

        return ResponseEntity.ok(
                claimService.reviewClaim(
                        claimId,
                        request));
    }
    
    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{claimId}/recommend-approve")
    public ResponseEntity<ClaimResponse>
    recommendApprove(
            @PathVariable Long claimId){

        return ResponseEntity.ok(
                claimService
                .recommendApprove(claimId));
    }
    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/{claimId}/recommend-reject")
    public ResponseEntity<ClaimResponse>
    recommendReject(
            @PathVariable Long claimId){

        return ResponseEntity.ok(
                claimService
                .recommendReject(claimId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{claimId}/approve")
    public ResponseEntity<ClaimResponse>
    approveClaim(
            @PathVariable Long claimId){

        return ResponseEntity.ok(
                claimService
                .approveClaim(claimId));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{claimId}/reject")
    public ResponseEntity<ClaimResponse>
    rejectClaim(
            @PathVariable Long claimId){

        return ResponseEntity.ok(
                claimService
                .rejectClaim(claimId));
    }
}