package com.insurance.demo.entity;

import java.time.LocalDateTime;

import com.insurance.demo.enums.ClaimStatus;

import jakarta.persistence.*;
import lombok.*;

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

    private String claimReason;

    private Double claimAmount;

    private LocalDateTime claimDate;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private Policy policy;
}