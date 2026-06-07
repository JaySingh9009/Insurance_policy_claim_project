package com.insurance.demo.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.ClaimRequest;
import com.insurance.demo.dto.ClaimResponse;
import com.insurance.demo.dto.ClaimReviewRequest;
import com.insurance.demo.entity.Claim;
import com.insurance.demo.entity.ClaimStatusHistory;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.ClaimService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl
        implements ClaimService {

    private final ClaimRepository claimRepository;

    private final PolicyRepository policyRepository;

    private final ClaimStatusHistoryRepository historyRepository;

    @Override
    public ClaimResponse submitClaim(
            ClaimRequest request) {

        Policy policy =
                policyRepository.findById(
                        request.getPolicyId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy Not Found"));

        if (policy.getStatus()
                != PolicyStatus.ACTIVE) {

            throw new RuntimeException(
                    "Only active policy can raise claim");
        }

        Claim claim =
                Claim.builder()
                .claimReason(
                        request.getClaimReason())
                .claimAmount(
                        request.getClaimAmount())
                .claimDate(
                        LocalDateTime.now())
                .status(
                        ClaimStatus.SUBMITTED)
                .policy(policy)
                .build();

        claimRepository.save(claim);

        return new ClaimResponse(
                claim.getClaimId(),
                policy.getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }

    @Override
    public List<ClaimResponse>
    getClaims(Long policyId) {

        return claimRepository
                .findByPolicyPolicyId(policyId)
                .stream()
                .map(claim ->
                        new ClaimResponse(
                                claim.getClaimId(),
                                claim.getPolicy()
                                        .getPolicyNumber(),
                                claim.getClaimAmount(),
                                claim.getStatus()
                                        .name()))
                .toList();
    }

    @Override
    public ClaimResponse reviewClaim(
            Long claimId,
            ClaimReviewRequest request) {

        Claim claim =
                claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimStatus oldStatus =
                claim.getStatus();

        claim.setStatus(
                ClaimStatus.UNDER_REVIEW);

        claimRepository.save(claim);

        ClaimStatusHistory history =
                ClaimStatusHistory.builder()
                .previousStatus(oldStatus)
                .newStatus(
                        ClaimStatus.UNDER_REVIEW)
                .remarks(
                        request.getRemarks())
                .updatedDate(
                        LocalDateTime.now())
                .claim(claim)
                .build();

        historyRepository.save(history);

        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicy()
                        .getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }

    @Override
    public ClaimResponse recommendApprove(
            Long claimId) {

        Claim claim =
                claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimStatus oldStatus =
                claim.getStatus();

        claim.setStatus(
                ClaimStatus.RECOMMENDED_FOR_APPROVAL);

        claimRepository.save(claim);

        ClaimStatusHistory history =
                ClaimStatusHistory.builder()
                .previousStatus(oldStatus)
                .newStatus(
                        ClaimStatus.RECOMMENDED_FOR_APPROVAL)
                .remarks(
                        "Recommended For Approval")
                .updatedDate(
                        LocalDateTime.now())
                .claim(claim)
                .build();

        historyRepository.save(history);

        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicy()
                        .getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }

    @Override
    public ClaimResponse recommendReject(
            Long claimId) {

        Claim claim =
                claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimStatus oldStatus =
                claim.getStatus();

        claim.setStatus(
                ClaimStatus.RECOMMENDED_FOR_REJECTION);

        claimRepository.save(claim);

        ClaimStatusHistory history =
                ClaimStatusHistory.builder()
                .previousStatus(oldStatus)
                .newStatus(
                        ClaimStatus.RECOMMENDED_FOR_REJECTION)
                .remarks(
                        "Recommended For Rejection")
                .updatedDate(
                        LocalDateTime.now())
                .claim(claim)
                .build();

        historyRepository.save(history);

        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicy()
                        .getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }

    @Override
    public ClaimResponse approveClaim(
            Long claimId) {

        Claim claim =
                claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimStatus oldStatus =
                claim.getStatus();

        claim.setStatus(
                ClaimStatus.APPROVED);

        claimRepository.save(claim);

        ClaimStatusHistory history =
                ClaimStatusHistory.builder()
                .previousStatus(oldStatus)
                .newStatus(
                        ClaimStatus.APPROVED)
                .remarks(
                        "Claim Approved")
                .updatedDate(
                        LocalDateTime.now())
                .claim(claim)
                .build();

        historyRepository.save(history);

        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicy()
                        .getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }

    @Override
    public ClaimResponse rejectClaim(
            Long claimId) {

        Claim claim =
                claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Claim Not Found"));

        ClaimStatus oldStatus =
                claim.getStatus();

        claim.setStatus(
                ClaimStatus.REJECTED);

        claimRepository.save(claim);

        ClaimStatusHistory history =
                ClaimStatusHistory.builder()
                .previousStatus(oldStatus)
                .newStatus(
                        ClaimStatus.REJECTED)
                .remarks(
                        "Claim Rejected")
                .updatedDate(
                        LocalDateTime.now())
                .claim(claim)
                .build();

        historyRepository.save(history);

        return new ClaimResponse(
                claim.getClaimId(),
                claim.getPolicy()
                        .getPolicyNumber(),
                claim.getClaimAmount(),
                claim.getStatus().name());
    }
}