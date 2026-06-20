package com.insurance.demo.dto;

import com.insurance.demo.enums.PaymentMethod;
import com.insurance.demo.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMethod paymentMode;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}