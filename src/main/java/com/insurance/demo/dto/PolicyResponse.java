package com.insurance.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyResponse {

    private Long policyId;
    private String policyNumber;
    private String customerName;
    private Long customerId;
    private String planName;
    private Long planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double totalPremiumPaid;
    private LocalDate lastPaymentDate;
    private LocalDate nextPaymentDueDate;
    private LocalDateTime createdAt;
}