package com.insurance.demo.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.PaymentRequest;
import com.insurance.demo.dto.PaymentResponse;
import com.insurance.demo.entity.Policy;
import com.insurance.demo.entity.PremiumPayment;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.repository.PaymentRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;

	private final PolicyRepository policyRepository;

	@Override
	public PaymentResponse makePayment(PaymentRequest request) {

		Policy policy = policyRepository
		        .findById(request.getPolicyId())
		        .orElseThrow(() ->
		                new RuntimeException(
		                        "Policy Not Found"));

		if(policy.getStatus() == PolicyStatus.ACTIVE){
		    throw new RuntimeException(
		            "Policy is already active");
		}

		PremiumPayment payment = PremiumPayment.builder().amount(policy.getPlan().getPremiumAmount())
				.paymentDate(LocalDateTime.now()).paymentMethod(request.getPaymentMethod())
				.status(PaymentStatus.SUCCESS).transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8))
				.policy(policy).build();

		paymentRepository.save(payment);

		policy.setStatus(PolicyStatus.ACTIVE);

		policyRepository.save(policy);

		return new PaymentResponse(payment.getPaymentId(), payment.getTransactionId(), payment.getStatus().name(),
				policy.getPolicyNumber());
	}
	
	@Override
	public List<PaymentResponse> getPaymentHistory(
	        Long policyId) {

	    return paymentRepository
	            .findByPolicyPolicyId(policyId)
	            .stream()
	            .map(payment ->
	                    new PaymentResponse(
	                            payment.getPaymentId(),
	                            payment.getTransactionId(),
	                            payment.getStatus().name(),
	                            payment.getPolicy()
	                                    .getPolicyNumber()))
	            .toList();
	}
	@Override
	public List<PaymentResponse> getAllPayments() {

	    return paymentRepository
	            .findAll()
	            .stream()
	            .map(payment ->
	                    new PaymentResponse(
	                            payment.getPaymentId(),
	                            payment.getTransactionId(),
	                            payment.getStatus().name(),
	                            payment.getPolicy()
	                                    .getPolicyNumber()))
	            .toList();
	}
}