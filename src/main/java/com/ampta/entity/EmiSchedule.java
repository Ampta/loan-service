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

    private Double principalAmount;

    private Double interestAmount;

    private Double interestRate;

    private Double openingBalance;

    private Double closingBalance;

    private Double emiAmount;

    private String paymentStatus;

    private LocalDateTime paidDate;

    private String cancellationReason;
}