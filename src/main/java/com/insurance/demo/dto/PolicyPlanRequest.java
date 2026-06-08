package com.insurance.demo.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PolicyPlanRequest {

	private String planName;
	@Positive
	private Double coverageAmount;

	@Positive
	private Double premiumAmount;
	
	@Positive
	private Integer durationInYears;

	private String termsAndConditions;

	private boolean active;

	private Long productId;
}