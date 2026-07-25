package com.insurance.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyPlanResponse {

    private Long planId;
    private String planName;
    private Double coverageAmount;
    private Double premiumAmount;
    private String premiumType;
    private java.util.Set<String> allowedPremiumTypes;
    private Integer durationInYears;
    private String termsAndConditions;
    private boolean active;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
}
