package com.insurance.demo.dto;

import lombok.Data;

@Data
public class ClaimRequest {

    private Long policyId;

    private String claimReason;

    private Double claimAmount;
}