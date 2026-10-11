package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "EmiSchedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emiScheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    private Long installmentNo;

    private LocalDate disbursementDate;

    private LocalDate dueDate;

    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double principalAmount;

    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double interestAmount;


    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double interestRate;


    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double openingBalance;


    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double closingBalance;


    @Column(columnDefinition = "DECIMAL(18,2)")
    private Double emiAmount;

    private String paymentStatus;

    private LocalDateTime paidDate;

    private String cancellationReason;
}