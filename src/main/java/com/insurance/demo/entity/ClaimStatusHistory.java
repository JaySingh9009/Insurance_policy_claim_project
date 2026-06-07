package com.insurance.demo.entity;

import java.time.LocalDateTime;

import com.insurance.demo.enums.ClaimStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "claim_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Enumerated(EnumType.STRING)
    private ClaimStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private ClaimStatus newStatus;

    private String remarks;

    private LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User updatedBy;
}