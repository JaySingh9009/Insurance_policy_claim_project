package com.insurance.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Used by Agent to move a claim to UNDER_REVIEW or recommend approve/reject.
 */
@Data
public class AgentRemarkRequest {

    @NotNull(message = "Target status is required")
    private String targetStatus; // UNDER_REVIEW | RECOMMENDED_APPROVAL | RECOMMENDED_REJECTION

    private String remarks;
}
