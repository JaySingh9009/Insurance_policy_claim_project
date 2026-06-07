package com.insurance.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "claim_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String documentName;

    private String documentType;

    private String documentReference;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    private Claim claim;
}