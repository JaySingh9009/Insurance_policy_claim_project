package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;
	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	public ResponseEntity<PaymentResponse> makePayment(@RequestBody PaymentRequest request) {

		return ResponseEntity.ok(paymentService.makePayment(request));
	}
	@GetMapping("/policy/{policyId}")
	public ResponseEntity<List<PaymentResponse>>
	getPaymentHistory(
	        @PathVariable Long policyId){

	    return ResponseEntity.ok(
	            paymentService
	            .getPaymentHistory(policyId));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<List<PaymentResponse>>
	getAllPayments(){

	    return ResponseEntity.ok(
	            paymentService.getAllPayments());
	}
}