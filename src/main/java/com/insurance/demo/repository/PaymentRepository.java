package com.insurance.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.PremiumPayment;

public interface PaymentRepository extends JpaRepository<PremiumPayment, Long> {

	List<PremiumPayment> findByPolicyPolicyId(Long policyId);
}