package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "loan_deal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "deal_id")
    private Long dealId;

//    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
//    @Column(name = "loan_type", length = 100)
    private LoanType loanType;

//    @Column(name = "loan_amount")
    private Double loanAmount;
//
//    @Column(name = "interest_rate")
    private Double interestRate;

//    @Column(name = "tenure_months")
    private Integer tenureMonths;

//    @Column(name = "emi_amount")
    private Double emiAmount;

//    @Column(name = "bank_name", length = 200)
    private String bankName;

//    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

//    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

//    @Column(name = "emi_day")
    private Integer emiDay;

//    @Column(name = "approved_amount")
    private Double approvedAmount;

    @Enumerated(EnumType.STRING)
//    @Column(name = "status", length = 50, nullable = false)
    private LoanStatus status;
}