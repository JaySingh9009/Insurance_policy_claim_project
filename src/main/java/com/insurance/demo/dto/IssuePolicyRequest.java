package com.insurance.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Used by Admin/Agent to issue a policy to a specific customer.
 */
@Data
public class IssuePolicyRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private LocalDate startDate;
}
