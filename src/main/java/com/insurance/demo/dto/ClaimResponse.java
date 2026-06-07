package com.insurance.demo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimResponse {

    private Long claimId;

    private String policyNumber;

    private Double claimAmount;

    private String status;
}