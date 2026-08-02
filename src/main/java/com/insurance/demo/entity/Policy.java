package com.insurance.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.enums.PremiumType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private PremiumType selectedPremiumType;

    @Column
    private Double installmentAmount;

    @Column(nullable = false)
    @Builder.Default
    private Double totalPremiumPaid = 0.0;

    @Column
    private LocalDate lastPaymentDate;

    @Column
    private LocalDate nextPaymentDueDate;

    // ── Motor-specific fields (null for non-MOTOR policies) ──────────────────
    @Column
    private String vehicleRegistrationNo;  // e.g. "MH12AB1234"

    @Column
    private String vehicleMakeModel;       // e.g. "Maruti Swift"

    @Column
    private Integer vehicleYear;           // manufacturing year e.g. 2019


    @Column
    private Double idvAmount;             // IRDA-depreciated Insured Declared Value

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}