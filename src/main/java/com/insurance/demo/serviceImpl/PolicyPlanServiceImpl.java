package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.PolicyPlanRequest;
import com.insurance.demo.dto.PolicyPlanResponse;
import com.insurance.demo.entity.InsuranceProduct;
import com.insurance.demo.entity.PolicyPlan;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.ProductRepository;
import com.insurance.demo.service.PolicyPlanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyPlanServiceImpl implements PolicyPlanService {

	private final PolicyPlanRepository planRepository;

	private final ProductRepository productRepository;

	@Override
	public PolicyPlanResponse createPlan(PolicyPlanRequest request) {

		InsuranceProduct product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new RuntimeException("Product not found"));

		PolicyPlan plan = PolicyPlan.builder().planName(request.getPlanName())
				.coverageAmount(request.getCoverageAmount()).premiumAmount(request.getPremiumAmount())
				.durationInYears(request.getDurationInYears()).termsAndConditions(request.getTermsAndConditions())
				.active(request.isActive()).product(product).build();

		planRepository.save(plan);

		return new PolicyPlanResponse(plan.getPlanId(), plan.getPlanName(), plan.getCoverageAmount(),
				plan.getPremiumAmount(), plan.getDurationInYears(), product.getProductName());
	}

	@Override
	public List<PolicyPlanResponse> getPlansByProduct(Long productId) {

		return planRepository.findByProductProductId(productId).stream()
				.map(plan -> new PolicyPlanResponse(plan.getPlanId(), plan.getPlanName(), plan.getCoverageAmount(),
						plan.getPremiumAmount(), plan.getDurationInYears(), plan.getProduct().getProductName()))
				.toList();
	}
}