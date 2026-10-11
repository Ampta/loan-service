package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalty_charges")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long penaltyChargeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmiScheduleId", nullable = false)
    private EmiSchedule emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    private Double penaltyAmount;

    private String reason;

    // UNPAID, PAID, WAIVED
    private String status = "UNPAID";

    @CreationTimestamp
    private LocalDateTime createdAt;
}