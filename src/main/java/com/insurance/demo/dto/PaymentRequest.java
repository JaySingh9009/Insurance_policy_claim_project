package com.insurance.demo.dto;

import com.insurance.demo.enums.PaymentMethod;

import lombok.Data;

@Data
public class PaymentRequest {

	private Long policyId;

	private PaymentMethod paymentMethod;
}