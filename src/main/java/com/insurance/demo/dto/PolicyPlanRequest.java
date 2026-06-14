package com.insurance.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PolicyPlanRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @NotNull(message = "Coverage amount is required")
    @Positive(message = "Coverage amount must be positive")
    private Double coverageAmount;

    @NotNull(message = "Premium amount is required")
    @Positive(message = "Premium amount must be positive")
    private Double premiumAmount;

    @NotBlank(message = "Premium type is required")
    private String premiumType;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationInYears;

    @NotBlank(message = "Terms and conditions are required")
    private String termsAndConditions;
}