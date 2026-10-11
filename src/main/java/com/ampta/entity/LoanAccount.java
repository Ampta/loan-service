package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "loan_accounts"
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        name = "UX_LoanAccounts_LoanAccountNo",
//                        columnNames = "LoanAccountNo"
//                )
//        },
//        indexes = {
//                @Index(
//                        name = "IX_LoanAccounts_CustomerId",
//                        columnList = "CustomerId"
//                ),
//                @Index(
//                        name = "IX_LoanAccounts_DealId",
//                        columnList = "DealId"
//                )
//        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer loanAccountId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_LoanAccounts_Customers"
            )
    )
    private User customer;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_LoanAccounts_LoanDeals"
            )
    )
    private LoanDeal loanDeal;

//    @Column(
//            length = 100,
//            nullable = false,
//            unique = true
//    )
    private String loanAccountNo;

//    @Column(
//            precision = 18,
//            scale = 2,
//            nullable = false
//    )
    private Double loanAmount;


//    @Column(
//            precision = 18,
//            scale = 2
//    )
    private Double outstandingPrincipal;


//    @Column(
//            length = 50
//    )
    private String loanStatus;


//    @Column(
//            precision = 8,
//            scale = 4
//    )
    private Double interestRate;


    private Integer tenureMonths;


//    @Column(
//            precision = 18,
//            scale = 2
//    )
    private Double emiAmount;


    private LocalDateTime disbursementDate;


//    @Column(
//            precision = 18,
//            scale = 2
//    )
    private Double totalPaidAmount;


//    @Column(
//            nullable = false,
//            updatable = false
//    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}