package com.insurance.demo.repository;

import com.insurance.demo.entity.Policy;
import com.insurance.demo.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Page<Policy> findAll(Pageable pageable);

    List<Policy> findByCustomerCustomerId(Long customerId);

    Page<Policy> findByCustomerCustomerId(Long customerId, Pageable pageable);

    boolean existsByPlan_Product_ProductId(Long productId);
}