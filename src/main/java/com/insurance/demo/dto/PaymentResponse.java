package com.insurance.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

	private Long paymentId;

	private String transactionId;

	private String status;

	private String policyNumber;
}