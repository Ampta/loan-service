package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "LoanPayments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmiScheduleId")
    private EmiSchedule emiSchedule;

    private Double paymentAmount;

    @CreationTimestamp
    private LocalDateTime paymentDate;

    // SUCCESS, FAILED, PENDING, REFUNDED
    private String paymentStatus = "PENDING";

    // EMI_PAYMENT, FORECLOSURE_PAYMENT, PARTIAL_PAYMENT, PENALTY_PAYMENT
    private String paymentName;

    // WALLET, NET_BANKING, UPI, CARD, CASH
    private String paymentMode = "ONLINE";



}