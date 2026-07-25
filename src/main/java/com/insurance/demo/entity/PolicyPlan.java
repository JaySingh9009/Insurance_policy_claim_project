package com.insurance.demo.entity;

import com.insurance.demo.enums.PremiumType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(nullable = false)
    private String planName;

    @Column(nullable = false)
    private Double coverageAmount;

    @Column(nullable = false)
    private Double premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PremiumType premiumType;

    @ElementCollection(targetClass = PremiumType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_allowed_premium_types", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private java.util.Set<PremiumType> allowedPremiumTypes = new java.util.HashSet<>();

    @Column(nullable = false)
    private Integer durationInYears;

    @Column(length = 2000)
    private String termsAndConditions;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}