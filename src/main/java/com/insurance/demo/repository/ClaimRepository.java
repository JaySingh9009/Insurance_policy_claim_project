package com.insurance.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.entity.Claim;

public interface ClaimRepository
        extends JpaRepository<Claim, Long>{

    List<Claim> findByPolicyPolicyId(
            Long policyId);
}