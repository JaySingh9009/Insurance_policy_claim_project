package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

	@PostMapping
	public ResponseEntity<PolicyResponse> purchasePolicy(@RequestBody PurchasePolicyRequest request) {

		return ResponseEntity.ok(policyService.purchasePolicy(request));
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<List<PolicyResponse>> getPolicies(@PathVariable Long customerId) {

		return ResponseEntity.ok(policyService.getPoliciesByCustomer(customerId));
	}
}