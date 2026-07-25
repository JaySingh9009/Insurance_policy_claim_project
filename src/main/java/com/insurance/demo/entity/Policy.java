package com.insurance.demo.entity;

import com.insurance.demo.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private PolicyPlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Enumerated(EnumType.STRING)
    private com.insurance.demo.enums.PremiumType selectedPremiumType;

    @Column
    private Double installmentAmount;

    @Column(nullable = false)
    @Builder.Default
    private Double totalPremiumPaid = 0.0;

    @Column
    private LocalDate lastPaymentDate;

    @Column
    private LocalDate nextPaymentDueDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}