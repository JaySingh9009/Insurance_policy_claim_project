package com.insurance.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

	List<Policy> findByCustomerCustomerId(Long customerId);
}