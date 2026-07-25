package com.insurance.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimResponse {

    private Long claimId;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private Double claimAmount;
    private String claimReason;
    private LocalDate incidentDate;
    private String status;
    private String agentRemarks;
    private String adminRemarks;
    private String customerName;
    private Long assignedAgentId;
    private String assignedAgentName;
    private Integer fraudRiskScore;
    private String fraudRiskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}