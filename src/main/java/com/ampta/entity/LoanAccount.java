package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_account_id")
    private Long loanAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private LoanDeal loanDeal;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "loan_account_no", nullable = false,
            unique = true, length = 100)
    private String loanAccountNo;

    @Column(name = "loan_amount")
    private Double loanAmount;

    @Column(name = "outstanding_principal")
    private Double outstandingPrincipal;

    @Column(name = "loan_status", length = 50)
    @Builder.Default
    private String loanStatus = "ACTIVE";

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "emi_amount")
    private Double emiAmount;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "total_paid_amount")
    @Builder.Default
    private Double totalPaidAmount = 0.0;

    @Column(name = "missed_emi_count")
    @Builder.Default
    private Integer missedEmiCount = 0;
}