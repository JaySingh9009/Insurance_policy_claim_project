package com.insurance.demo.service;

import com.insurance.demo.dto.PagedResponse;
import com.insurance.demo.dto.PolicyPlanRequest;
import com.insurance.demo.dto.PolicyPlanResponse;

public interface PolicyPlanService {
    PolicyPlanResponse createPlan(PolicyPlanRequest request);
    PolicyPlanResponse updatePlan(Long id, PolicyPlanRequest request);
    PolicyPlanResponse getPlanById(Long id);
    PagedResponse<PolicyPlanResponse> getActivePlans(int page, int size, String sortBy, String sortDir);
    PagedResponse<PolicyPlanResponse> getPlansByProduct(Long productId, int page, int size);
    void deactivatePlan(Long id);
    void activatePlan(Long id);
}