package com.insurance.demo.service;

import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;

public interface PaymentService {

	PaymentResponse makePayment(PaymentRequest request);
}