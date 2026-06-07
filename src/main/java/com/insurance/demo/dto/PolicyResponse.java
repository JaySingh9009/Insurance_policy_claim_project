package com.insurance.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyResponse {

	private Long policyId;

	private String policyNumber;

	private String customerName;

	private String planName;

	private String status;
}