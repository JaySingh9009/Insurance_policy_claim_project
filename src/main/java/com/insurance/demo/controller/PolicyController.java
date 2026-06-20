package com.insurance.demo.controller;

import java.util.List;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.PolicyResponse;
import com.insurance.demo.dto.PurchasePolicyRequest;
import com.insurance.demo.service.PolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

	private final PolicyService policyService;

	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	public ResponseEntity<PolicyResponse> purchasePolicy(@RequestBody PurchasePolicyRequest request) {

		return ResponseEntity.ok(policyService.purchasePolicy(request));
	}

	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','AGENT')")
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<List<PolicyResponse>> getPolicies(@PathVariable Long customerId) {

		return ResponseEntity.ok(policyService.getPoliciesByCustomer(customerId));
	}

	@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
	@PutMapping("/{policyId}/issue")
	public ResponseEntity<PolicyResponse> issuePolicy(@PathVariable Long policyId) {

		return ResponseEntity.ok(policyService.issuePolicy(policyId));
	}

	@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
	@PutMapping("/{policyId}/cancel")
	public ResponseEntity<PolicyResponse> cancelPolicy(@PathVariable Long policyId) {

		return ResponseEntity.ok(policyService.cancelPolicy(policyId));
	}

	@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
	@GetMapping
	public ResponseEntity<List<PolicyResponse>> getAllPolicies() {

		return ResponseEntity.ok(policyService.getAllPolicies());
	}

	@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
	@GetMapping("/{policyId}")
	public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long policyId) {

		return ResponseEntity.ok(policyService.getPolicyById(policyId));
	}
	
	@GetMapping("/my-policies")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<List<PolicyResponse>>
	getMyPolicies(
	        Authentication authentication) {

	    return ResponseEntity.ok(
	            policyService.getMyPolicies(
	                    authentication.getName()));
	}
}