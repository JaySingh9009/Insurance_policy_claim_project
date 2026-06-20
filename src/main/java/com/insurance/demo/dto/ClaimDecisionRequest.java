package com.insurance.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Used by Admin to make a final APPROVED or REJECTED decision on a claim.
 */
@Data
public class ClaimDecisionRequest {

    @NotNull(message = "Decision is required")
    private String decision; // APPROVED | REJECTED

    private String adminRemarks;
}
