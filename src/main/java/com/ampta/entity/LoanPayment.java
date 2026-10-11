package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
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
    @Column(name = "PaymentId")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmiScheduleId")
    private EmiSchedule emiSchedule;

    @Column(name = "PaymentAmount", precision = 18, scale = 2)
    private BigDecimal paymentAmount;

    @CreationTimestamp
    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus = "SUCCESS"; // SUCCESS, FAILED, PENDING, REFUNDED

    @Column(name = "PaymentName", length = 200)
    private String paymentName; // EMI_PAYMENT, FORECLOSURE_PAYMENT, PARTIAL_PAYMENT, PENALTY_PAYMENT

    @Column(name = "PaymentMode", length = 50)
    private String paymentMode = "ONLINE"; // WALLET, NET_BANKING, UPI, CARD, CASH
}

