package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.ClaimHistoryResponse;

public interface ClaimHistoryService {

    List<ClaimHistoryResponse>
    getClaimHistory(Long claimId);
}