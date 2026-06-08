package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.PolicyPlanRequest;
import com.insurance.demo.dto.PolicyPlanResponse;
import com.insurance.demo.service.PolicyPlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PolicyPlanController {

	private final PolicyPlanService planService;
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<PolicyPlanResponse> createPlan(@RequestBody PolicyPlanRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<PolicyPlanResponse>> getPlans(@PathVariable Long productId) {

		return ResponseEntity.ok(planService.getPlansByProduct(productId));
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{planId}")
	public ResponseEntity<PolicyPlanResponse>
	updatePlan(
	        @PathVariable Long planId,
	        @RequestBody PolicyPlanRequest request){

	    return ResponseEntity.ok(
	            planService.updatePlan(
	                    planId,
	                    request));
	}
	
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{planId}")
	public ResponseEntity<PolicyPlanResponse>
	getPlanById(
	        @PathVariable Long planId){

	    return ResponseEntity.ok(
	            planService.getPlanById(planId));
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{planId}/deactivate")
	public ResponseEntity<String>
	deactivatePlan(
	        @PathVariable Long planId){

	    planService.deactivatePlan(planId);

	    return ResponseEntity.ok(
	            "Plan deactivated successfully");
	}
}