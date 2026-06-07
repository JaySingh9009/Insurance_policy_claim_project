package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.ClaimRequest;
import com.insurance.demo.dto.ClaimResponse;
import com.insurance.demo.dto.ClaimReviewRequest;

public interface ClaimService {

    ClaimResponse submitClaim(
            ClaimRequest request);

    List<ClaimResponse> getClaims(
            Long policyId);
    ClaimResponse reviewClaim(
            Long claimId,
            ClaimReviewRequest request);
    ClaimResponse recommendApprove(
            Long claimId);
    
    ClaimResponse recommendReject(
            Long claimId);
    
    ClaimResponse approveClaim(
            Long claimId);
    
    ClaimResponse rejectClaim(
            Long claimId);
}