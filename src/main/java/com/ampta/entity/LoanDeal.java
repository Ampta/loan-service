package com.ampta.entity;

import com.ampta.entity.enums.LoanStatus;
import com.ampta.entity.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_deal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDeal  extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long dealId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    private Double loanAmount;

    private Double interestRate;

    private Integer tenureMonths;

    private Double emiAmount;

    private String bankName;

    private String bankAccountNumber;

    private String ifscCode;

    private Integer emiDay;

    private Double approvedAmount;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;
}