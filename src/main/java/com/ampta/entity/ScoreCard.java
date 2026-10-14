package com.ampta.entity;

import com.ampta.entity.enums.RiskCategory;
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
public class ScoreCard extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`ScoreCardId`")
    private Integer scoreCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`CustomerId`", nullable = false)
    private Customer customer;

    @Column(name = "`CurrentStatus`", length = 100)
    private String currentStatus;

    @Column(name = "`RejectionReason`", length = 1000)
    private String rejectionReason;

    @Column(name = "`AppliedDate`")
    private LocalDateTime appliedDate;

    @Column(name = "`CibilScore`")
    private Integer cibilScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "`RiskCategory`", length = 50)
    private RiskCategory riskCategory;

    @Column(
            name = "`EligibleLoanAmount`",
            columnDefinition = "DECIMAL(18,2)"
    )
    private Double eligibleLoanAmount;
}