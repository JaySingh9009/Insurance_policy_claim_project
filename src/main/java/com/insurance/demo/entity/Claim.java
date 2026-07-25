package com.insurance.demo.entity;

import com.insurance.demo.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @Column(unique = true, nullable = false)
    private String claimNumber;

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private Double claimAmount;

    @Column(nullable = false, length = 1000)
    private String claimReason;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    private String agentRemarks;

    private String adminRemarks;


    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    @Column(nullable = false)
    @Builder.Default
    private Integer fraudRiskScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private String fraudRiskLevel = "LOW";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}