package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.PolicyResponse;
import com.insurance.demo.dto.PurchasePolicyRequest;

public interface PolicyService {

	PolicyResponse purchasePolicy(PurchasePolicyRequest request);

	List<PolicyResponse> getPoliciesByCustomer(Long customerId);

	PolicyResponse issuePolicy(Long policyId);

	PolicyResponse cancelPolicy(Long policyId);

	PolicyResponse getPolicyById(Long policyId);

	List<PolicyResponse> getAllPolicies();

	List<PolicyResponse> getMyPolicies(String email);

}