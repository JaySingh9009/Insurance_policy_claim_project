package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.ClaimHistoryResponse;
import com.insurance.demo.service.ClaimHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claim-history")
@RequiredArgsConstructor
public class ClaimHistoryController {

    private final ClaimHistoryService
            claimHistoryService;

    @GetMapping("/{claimId}")
    public ResponseEntity<List<ClaimHistoryResponse>>
    getHistory(
            @PathVariable Long claimId){

        return ResponseEntity.ok(
                claimHistoryService
                .getClaimHistory(claimId));
    }
}