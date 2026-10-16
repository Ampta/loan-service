package com.ampta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "closure_requests")
@Getter
@Setter
public class ClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Version
    private Long version;

    private String rejectionReason;

    @Column(unique = true)
    private String paymentReference;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime paidAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}