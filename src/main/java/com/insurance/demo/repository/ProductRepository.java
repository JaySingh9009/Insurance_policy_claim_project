package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.InsuranceProduct;

public interface ProductRepository extends JpaRepository<InsuranceProduct, Long> {

	boolean existsByProductName(String productName);
}