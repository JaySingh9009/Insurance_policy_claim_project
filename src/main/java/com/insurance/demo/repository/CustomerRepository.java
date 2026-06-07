package com.insurance.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>{

    Optional<Customer> findByUser_Id(Long id);
}