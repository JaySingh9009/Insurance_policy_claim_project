package com.insurance.demo.serviceImpl;

import java.time.LocalDateTime;
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

		Policy policy = policyRepository.findById(request.getPolicyId())
				.orElseThrow(() -> new RuntimeException("Policy Not Found"));

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
}