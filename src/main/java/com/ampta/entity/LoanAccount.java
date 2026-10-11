package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer loanAccountId;

    // Customer reference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_LoanAccounts_Customers"
            )
    )
    private User customer;


    // Loan Deal reference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_LoanAccounts_LoanDeals"
            )
    )
    private LoanDeal loanDeal;

    private String loanAccountNo;

    private Double loanAmount;

    private Double outstandingPrincipal;

    private String loanStatus;

    private Double interestRate;

    private Integer tenureMonths;

    private Double emiAmount;

    private LocalDateTime disbursementDate; //  +  plus one month next month se emiday

    private Double totalPaidAmount;

}
