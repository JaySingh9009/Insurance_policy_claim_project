package com.insurance.demo.dto;

import lombok.Data;

@Data
public class PurchasePolicyRequest {

	private Long customerId;

	private Long planId;
}