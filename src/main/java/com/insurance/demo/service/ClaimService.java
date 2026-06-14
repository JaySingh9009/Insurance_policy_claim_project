package com.insurance.demo.service;

import com.insurance.demo.dto.*;

public interface ClaimService {
    ClaimResponse submitClaim(ClaimRequest request, Long userId);
    ClaimResponse updateClaimStatus(Long claimId, AgentRemarkRequest request, Long agentUserId);
    ClaimResponse makeClaimDecision(Long claimId, ClaimDecisionRequest request, Long adminUserId);
    ClaimResponse getClaimById(Long claimId);
    PagedResponse<ClaimResponse> getAllClaims(int page, int size, String sortBy, String sortDir);
    PagedResponse<ClaimResponse> getMyClaims(Long userId, int page, int size, String sortBy, String sortDir);
    PagedResponse<ClaimHistoryResponse> getClaimHistory(Long claimId, int page, int size);
}