package com.insurance.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PurchasePolicyRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private String selectedPremiumType;

    /**
     * startDate is optional — defaults to today if not provided.
     * Must be present or future if provided.
     */
    private LocalDate startDate;
}