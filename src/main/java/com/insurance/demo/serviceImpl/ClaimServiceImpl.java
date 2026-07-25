package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.*;
import com.insurance.demo.entity.*;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.*;
import com.insurance.demo.repository.*;
import com.insurance.demo.service.ClaimDocumentService;
import com.insurance.demo.service.ClaimService;
import com.insurance.demo.util.NumberGenerator;
import com.insurance.demo.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "status", "claimAmount", "incidentDate");

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository historyRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ClaimDocumentService claimDocumentService;



    @Override
    @Transactional
    public ClaimResponse submitClaim(ClaimRequest request, Long userId) {
        log.info("Submitting claim for policyId={} by userId={}", request.getPolicyId(), userId);

        // Extract customer from JWT — never from request
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BadRequestException("You must have a customer profile to submit a claim"));

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + request.getPolicyId()));

        // Policy must belong to this customer
        if (!policy.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new UnauthorizedAccessException("You are not authorized to raise a claim on this policy");
        }

        // Auto-lapse check: check if past nextPaymentDueDate by > 30 days
        if (policy.getStatus() == PolicyStatus.ACTIVE && policy.getNextPaymentDueDate() != null) {
            if (LocalDate.now().isAfter(policy.getNextPaymentDueDate().plusDays(30))) {
                policy.setStatus(PolicyStatus.LAPSED);
                policyRepository.save(policy);
                log.info("Policy status automatically updated to LAPSED: policyId={}", policy.getPolicyId());
            }
        }

        // Policy must be ACTIVE
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            log.warn("Claim rejected - policy is not active: policyId={}, status={}", policy.getPolicyId(), policy.getStatus());
            throw new InvalidPolicyStatusException(
                    "Claims can only be raised on ACTIVE policies. Policy status: " + policy.getStatus());
        }

        // Incident date validation against policy coverage period
        if (request.getIncidentDate().isBefore(policy.getStartDate()) || 
            request.getIncidentDate().isAfter(policy.getEndDate())) {
            throw new BadRequestException("Incident date must fall within the policy coverage period (" + 
                policy.getStartDate() + " to " + policy.getEndDate() + ")");
        }

        // Incident date validation against initial activation/payment date
        if (policy.getLastPaymentDate() != null && request.getIncidentDate().isBefore(policy.getLastPaymentDate())) {
            throw new BadRequestException("Incident date cannot be prior to the policy payment/activation date");
        }

        // Incident date validation against future date
        if (request.getIncidentDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Incident date must not be a future date");
        }


        List<Claim> policyClaims = claimRepository.findByPolicyPolicyId(policy.getPolicyId());
        double totalPreviousClaimed = policyClaims.stream()
                .filter(c -> c.getStatus() != ClaimStatus.REJECTED)
                .mapToDouble(Claim::getClaimAmount)
                .sum();

        if (totalPreviousClaimed + request.getClaimAmount() > policy.getPlan().getCoverageAmount()) {
            double remainingCoverage = Math.max(0.0, policy.getPlan().getCoverageAmount() - totalPreviousClaimed);
            log.warn("Total claimed amount ({}) + new claim ({}) exceeds policy coverage ({})", 
                    totalPreviousClaimed, request.getClaimAmount(), policy.getPlan().getCoverageAmount());
            throw new ClaimAmountExceededException(
                    "Total claimed amount would exceed the policy coverage limit. Remaining coverage: " + remainingCoverage + 
                    " (Coverage: " + policy.getPlan().getCoverageAmount() + ", Previously claimed: " + totalPreviousClaimed + ")");
        }

        // Cannot raise a second active claim (enhancement per spec)
        boolean hasActiveClaim = claimRepository.existsByPolicyPolicyIdAndStatusNotIn(
                policy.getPolicyId(), List.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED));
        if (hasActiveClaim) {
            log.warn("Duplicate active claim for policyId={}", policy.getPolicyId());
            throw new BadRequestException("An active claim already exists for this policy. Wait for it to be resolved before submitting a new one.");
        }

        // At least one document must be submitted (validated by @Size(min=1) on DTO, but double-check)
        if (request.getDocuments() == null || request.getDocuments().isEmpty()) {
            throw new BadRequestException("At least one claim document must be submitted");
        }

        // Compute Fraud Risk Score
        int riskScore = 0;
        if (request.getClaimAmount() > (policy.getPlan().getCoverageAmount() * 0.8)) {
            riskScore += 30;
            log.info("Fraud Risk Score: +30 points because claim amount {} exceeds 80% of coverage {}", 
                    request.getClaimAmount(), policy.getPlan().getCoverageAmount());
        }
        List<Claim> customerClaims = claimRepository.findByPolicyCustomerCustomerId(customer.getCustomerId());
        boolean hasPriorRejected = customerClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.REJECTED);
        if (hasPriorRejected) {
            riskScore += 30;
            log.info("Fraud Risk Score: +30 points because customer has prior rejected claims");
        }

        String riskLevel = "LOW";
        if (riskScore >= 70) {
            riskLevel = "HIGH";
        } else if (riskScore >= 40) {
            riskLevel = "MEDIUM";
        }

        Claim claim = Claim.builder()
                .claimNumber(NumberGenerator.generateClaimNumber())
                .policy(policy)
                .claimAmount(request.getClaimAmount())
                .claimReason(request.getClaimReason())
                .incidentDate(request.getIncidentDate())
                .status(ClaimStatus.SUBMITTED)
                .fraudRiskScore(riskScore)
                .fraudRiskLevel(riskLevel)
                .build();

        claim = claimRepository.save(claim);
        final Claim savedClaim = claim;

        // Link the pre-uploaded Cloudinary documents (from Step 1) to this claim.
        // Documents already exist in Cloudinary — we just store the URLs in DB.
        claimDocumentService.linkDocumentsToClaim(savedClaim.getClaimId(), request.getDocuments());

        // Record initial status history
        saveHistory(savedClaim, null, ClaimStatus.SUBMITTED, "Claim submitted by customer", customer.getUser());

        log.info("Claim submitted: claimId={}, claimNumber={}", savedClaim.getClaimId(), savedClaim.getClaimNumber());
        return mapToResponse(savedClaim);
    }

    // ─── Agent Status Update ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ClaimResponse updateClaimStatus(Long claimId, AgentRemarkRequest request, Long agentUserId) {
        log.info("Agent userId={} updating claimId={} to status={}", agentUserId, claimId, request.getTargetStatus());

        Claim claim = findClaim(claimId);
        User agent = findUser(agentUserId);


        ClaimStatus targetStatus = parseStatus(request.getTargetStatus());


        validateAgentTransition(claim.getStatus(), targetStatus);

        ClaimStatus previousStatus = claim.getStatus();

        if (targetStatus == ClaimStatus.UNDER_REVIEW) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        } else if (targetStatus == ClaimStatus.RECOMMENDED_APPROVAL) {
            claim.setStatus(ClaimStatus.RECOMMENDED_APPROVAL);
            claim.setAgentRemarks(request.getRemarks());
        } else if (targetStatus == ClaimStatus.RECOMMENDED_REJECTION) {
            claim.setStatus(ClaimStatus.RECOMMENDED_REJECTION);
            claim.setAgentRemarks(request.getRemarks());
        }

        claimRepository.save(claim);
        saveHistory(claim, previousStatus, targetStatus, request.getRemarks(), agent);

        log.info("Claim {} updated from {} to {} by agent {}", claimId, previousStatus, targetStatus, agentUserId);
        return mapToResponse(claim);
    }



    @Override
    @Transactional
    public ClaimResponse makeClaimDecision(Long claimId, ClaimDecisionRequest request, Long adminUserId) {
        log.info("Admin userId={} making decision on claimId={}: {}", adminUserId, claimId, request.getDecision());

        Claim claim = findClaim(claimId);
        User admin = findUser(adminUserId);

        // Cannot change terminal statuses
        if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new ClaimAlreadyFinalizedException("Claim is already finalized with status: " + claim.getStatus());
        }

        ClaimStatus decision = parseStatus(request.getDecision());
        validateAdminDecision(claim.getStatus(), decision);

        ClaimStatus previousStatus = claim.getStatus();
        claim.setStatus(decision);
        claim.setAdminRemarks(request.getAdminRemarks());

        claimRepository.save(claim);
        saveHistory(claim, previousStatus, decision, request.getAdminRemarks(), admin);

        log.info("Admin decision recorded: claimId={}, decision={}", claimId, decision);
        return mapToResponse(claim);
    }



    @Override
    public ClaimResponse getClaimById(Long claimId) {
        return mapToResponse(findClaim(claimId));
    }

    @Override
    public PagedResponse<ClaimResponse> getAllClaims(int page, int size, String sortBy, String sortDir) {
        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<Claim> claimPage = claimRepository.findAll(pageable);
        return toPagedResponse(claimPage);
    }

    @Override
    public PagedResponse<ClaimResponse> getMyClaims(Long userId, int page, int size, String sortBy, String sortDir) {
        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);

        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<Claim> claimPage = claimRepository.findByPolicyCustomerCustomerId(customer.getCustomerId(), pageable);
        return toPagedResponse(claimPage);
    }

    @Override
    public PagedResponse<ClaimHistoryResponse> getClaimHistory(Long claimId, int page, int size) {
        PaginationValidator.validate(page, size, "updatedAt", Set.of("updatedAt"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").ascending());
        List<ClaimStatusHistory> history = historyRepository.findByClaimClaimId(claimId);

        List<ClaimHistoryResponse> records = history.stream().map(h -> ClaimHistoryResponse.builder()
                .historyId(h.getHistoryId())
                .previousStatus(h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null)
                .newStatus(h.getNewStatus().name())
                .remarks(h.getRemarks())
                .updatedBy(h.getUpdatedBy() != null ? h.getUpdatedBy().getFullName() : "System")
                .updatedAt(h.getUpdatedAt())
                .build()).toList();

        return PagedResponse.<ClaimHistoryResponse>builder()
                .records(records)
                .currentPage(page)
                .pageSize(size)
                .totalRecords(records.size())
                .totalPages(1)
                .isLastPage(true)
                .build();
    }




    private void validateAgentTransition(ClaimStatus current, ClaimStatus target) {
        boolean valid = false;
        if (current == ClaimStatus.SUBMITTED) {
            valid = (target == ClaimStatus.UNDER_REVIEW);
        } else if (current == ClaimStatus.UNDER_REVIEW) {
            valid = (target == ClaimStatus.RECOMMENDED_APPROVAL || target == ClaimStatus.RECOMMENDED_REJECTION);
        }
        if (!valid) {
            throw new InvalidClaimStatusTransitionException(
                    "Agent cannot transition claim from " + current + " to " + target);
        }
    }

    /**
     * Strictly enforced admin decisions:
     * RECOMMENDED_APPROVAL → APPROVED | REJECTED
     * RECOMMENDED_REJECTION → APPROVED | REJECTED
     */
    private void validateAdminDecision(ClaimStatus current, ClaimStatus target) {
        boolean valid = false;
        if (current == ClaimStatus.RECOMMENDED_APPROVAL || current == ClaimStatus.RECOMMENDED_REJECTION) {
            valid = (target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED);
        }
        if (!valid) {
            throw new InvalidClaimStatusTransitionException(
                    "Admin cannot transition claim from " + current + " to " + target +
                    ". Claim must be in RECOMMENDED_APPROVAL or RECOMMENDED_REJECTION state first.");
        }
    }



    private void saveHistory(Claim claim, ClaimStatus previous, ClaimStatus next, String remarks, User updatedBy) {
        ClaimStatusHistory history = ClaimStatusHistory.builder()
                .claim(claim)
                .previousStatus(previous)
                .newStatus(next)
                .remarks(remarks)
                .updatedBy(updatedBy)
                .build();
        historyRepository.save(history);
    }

    private Claim findClaim(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private ClaimStatus parseStatus(String status) {
        try {
            return ClaimStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid claim status: " + status);
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<ClaimResponse> toPagedResponse(Page<Claim> page) {
        List<ClaimResponse> records = page.getContent().stream().map(this::mapToResponse).toList();
        return PagedResponse.<ClaimResponse>builder()
                .records(records)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public ClaimResponse assignAgent(Long claimId, Long agentId) {
        log.info("Admin assigning agentId={} to claimId={}", agentId, claimId);
        Claim claim = findClaim(claimId);
        User agent = findUser(agentId);

        if (agent.getRole() != com.insurance.demo.enums.Role.AGENT) {
            throw new BadRequestException("User with ID " + agentId + " is not an AGENT");
        }

        claim.setAssignedAgent(agent);
        claimRepository.save(claim);

        // Record history
        saveHistory(claim, claim.getStatus(), claim.getStatus(), "Claim assigned to agent: " + agent.getFullName(), null);

        log.info("Claim {} successfully assigned to agent {}", claimId, agentId);
        return mapToResponse(claim);
    }

    private ClaimResponse mapToResponse(Claim c) {
        return ClaimResponse.builder()
                .claimId(c.getClaimId())
                .claimNumber(c.getClaimNumber())
                .policyId(c.getPolicy().getPolicyId())
                .policyNumber(c.getPolicy().getPolicyNumber())
                .claimAmount(c.getClaimAmount())
                .claimReason(c.getClaimReason())
                .incidentDate(c.getIncidentDate())
                .status(c.getStatus().name())
                .agentRemarks(c.getAgentRemarks())
                .adminRemarks(c.getAdminRemarks())
                .customerName(c.getPolicy().getCustomer().getUser().getFullName())
                .assignedAgentId(c.getAssignedAgent() != null ? c.getAssignedAgent().getId() : null)
                .assignedAgentName(c.getAssignedAgent() != null ? c.getAssignedAgent().getFullName() : null)
                .fraudRiskScore(c.getFraudRiskScore())
                .fraudRiskLevel(c.getFraudRiskLevel())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}