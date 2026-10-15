package com.ampta.entity;

import com.ampta.entity.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "KycDocuments")
@Data
public class KycDocument extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String documentType;

    private String filePath;

    @Enumerated(EnumType.STRING)
    private DocumentStatus verificationStatus;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}