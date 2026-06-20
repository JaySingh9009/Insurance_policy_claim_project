package com.insurance.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be positive")
    private Double claimAmount;

    @NotBlank(message = "Claim reason is required")
    private String claimReason;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date must not be a future date")
    private LocalDate incidentDate;

    @NotNull(message = "At least one document is required")
    @Size(min = 1, message = "At least one document must be submitted with the claim")
    private List<ClaimDocumentRequest> documents;
}