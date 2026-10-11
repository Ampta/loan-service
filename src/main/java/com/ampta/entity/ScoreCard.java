package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "score_cards")
public class ScoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    private Long userId;
    private String currentStatus; // APPROVED, REJECTED, UNDER_REVIEW
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "applied_date")
    private LocalDateTime appliedDate;
    private Integer cibilScore;
    private String riskCategory;
    private Double eligibleLoanAmount;
}
