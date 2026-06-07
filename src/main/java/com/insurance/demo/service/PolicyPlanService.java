package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.PolicyPlanRequest;
import com.insurance.demo.dto.PolicyPlanResponse;

public interface PolicyPlanService {

	PolicyPlanResponse createPlan(PolicyPlanRequest request);

	List<PolicyPlanResponse> getPlansByProduct(Long productId);
}