package com.insurance.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyPlanResponse {

	private Long planId;

	private String planName;

	private Double coverageAmount;

	private Double premiumAmount;

	private Integer durationInYears;

	private String productName;
}
