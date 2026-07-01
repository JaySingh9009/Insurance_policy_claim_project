package com.insurance.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(nullable = false)
    private String documentName;

    /** e.g. PDF, IMAGE, MEDICAL_REPORT */
    @Column(nullable = false)
    private String documentType;

    /** Cloudinary HTTPS URL — publicly accessible */
    @Column(nullable = false, length = 1000)
    private String documentUrl;

    /** Cloudinary public_id — used for deletion */
    @Column(nullable = false)
    private String publicId;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}