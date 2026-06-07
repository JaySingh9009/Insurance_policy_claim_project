package com.insurance.demo.dto;

import lombok.Data;

@Data
public class PolicyPlanRequest {

	private String planName;

	private Double coverageAmount;

	private Double premiumAmount;

	private Integer durationInYears;

	private String termsAndConditions;

	private boolean active;

	private Long productId;
}