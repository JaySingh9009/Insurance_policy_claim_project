package com.insurance.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyRazorpayPaymentRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotNull(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    private String razorpaySignature;

    private Double amount;

    private String paymentMethod;
}
