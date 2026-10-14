
package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "loan_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_LoanAccounts_AccountNo",
                        columnNames = "loan_account_no"
                )
        }
)
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

    // Many loan accounts can belong to one customer
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            referencedColumnName = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_LoanAccounts_Customer")
    )
    private Customer customer;

    // Many loan accounts can belong to one loan deal
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            referencedColumnName = "deal_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_LoanAccounts_LoanDeal")
    )
    private LoanDeal loanDeal;

    @Column(name = "loan_account_no", nullable = false, length = 100)
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
