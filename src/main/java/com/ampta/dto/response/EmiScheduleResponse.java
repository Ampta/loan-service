package com.ampta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EmiScheduleResponse {
    private String loanAccountId;
    private Long emiScheduleId;
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

}
