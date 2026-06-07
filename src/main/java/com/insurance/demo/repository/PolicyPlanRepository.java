package com.insurance.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.PolicyPlan;

public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {

	List<PolicyPlan> findByProductProductId(Long productId);
}
