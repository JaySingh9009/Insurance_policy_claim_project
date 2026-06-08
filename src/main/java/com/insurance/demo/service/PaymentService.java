package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;

public interface PaymentService {

	PaymentResponse makePayment(PaymentRequest request);
	
	List<PaymentResponse> getPaymentHistory(
	        Long policyId);

	List<PaymentResponse> getAllPayments();
	
}