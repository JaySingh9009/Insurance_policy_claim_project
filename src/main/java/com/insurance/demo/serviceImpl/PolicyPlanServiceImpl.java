package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PolicyPlanRequest;
import com.insurance.demo.dto.PolicyPlanResponse;
import com.insurance.demo.entity.InsuranceProduct;
import com.insurance.demo.entity.PolicyPlan;
import com.insurance.demo.enums.PremiumType;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.ProductRepository;
import com.insurance.demo.service.PolicyPlanService;
import com.insurance.demo.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyPlanServiceImpl implements PolicyPlanService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("planName", "premiumAmount", "coverageAmount", "createdAt");

    private final PolicyPlanRepository planRepository;
    private final ProductRepository productRepository;

    @Override
    public PolicyPlanResponse createPlan(PolicyPlanRequest request) {
        log.info("Creating plan for productId={}", request.getProductId());

        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (!product.isActive()) {
            log.warn("Cannot add plan to inactive product: productId={}", product.getProductId());
            throw new BadRequestException("Cannot add a plan to an inactive product");
        }

        // Business rule: coverage must be greater than premium
        if (request.getCoverageAmount() <= request.getPremiumAmount()) {
            throw new BadRequestException("Coverage amount must be greater than premium amount");
        }

        PremiumType premiumType = parsePremiumType(request.getPremiumType());

        PolicyPlan plan = PolicyPlan.builder()
                .product(product)
                .planName(request.getPlanName())
                .coverageAmount(request.getCoverageAmount())
                .premiumAmount(request.getPremiumAmount())
                .premiumType(premiumType)
                .durationInYears(request.getDurationInYears())
                .termsAndConditions(request.getTermsAndConditions())
                .active(true)
                .build();

        plan = planRepository.save(plan);
        log.info("Plan created: planId={}", plan.getPlanId());
        return mapToResponse(plan);
    }



    @Override
    public PagedResponse<PolicyPlanResponse> getActivePlans(int page, int size, String sortBy, String sortDir) {
        PaginationValidator.validate(page, size, sortBy, ALLOWED_SORT_FIELDS);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<PolicyPlan> planPage = planRepository.findByActiveTrue(pageable);
        return toPagedResponse(planPage);
    }



    @Override
    public void deactivatePlan(Long id) {
        PolicyPlan plan = findPlan(id);
        plan.setActive(false);
        planRepository.save(plan);
        log.info("Plan deactivated: planId={}", id);
    }

    @Override
    public void activatePlan(Long id) {
        PolicyPlan plan = findPlan(id);
        plan.setActive(true);
        planRepository.save(plan);
        log.info("Plan activated: planId={}", id);
    }

    private PolicyPlan findPlan(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + id));
    }

    private PremiumType parsePremiumType(String type) {
        try {
            return PremiumType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid premium type: " + type + ". Valid values: ONE_TIME, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL");
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<PolicyPlanResponse> toPagedResponse(Page<PolicyPlan> planPage) {
        List<PolicyPlanResponse> records = planPage.getContent().stream().map(this::mapToResponse).toList();
        return PagedResponse.<PolicyPlanResponse>builder()
                .records(records)
                .currentPage(planPage.getNumber())
                .pageSize(planPage.getSize())
                .totalRecords(planPage.getTotalElements())
                .totalPages(planPage.getTotalPages())
                .isLastPage(planPage.isLast())
                .build();
    }

    private PolicyPlanResponse mapToResponse(PolicyPlan p) {
        return PolicyPlanResponse.builder()
                .planId(p.getPlanId())
                .planName(p.getPlanName())
                .coverageAmount(p.getCoverageAmount())
                .premiumAmount(p.getPremiumAmount())
                .premiumType(p.getPremiumType() != null ? p.getPremiumType().name() : "ANNUAL")
                .durationInYears(p.getDurationInYears())
                .termsAndConditions(p.getTermsAndConditions())
                .active(p.isActive())
                .productId(p.getProduct().getProductId())
                .productName(p.getProduct().getProductName())
                .createdAt(p.getCreatedAt())
                .build();
    }
}