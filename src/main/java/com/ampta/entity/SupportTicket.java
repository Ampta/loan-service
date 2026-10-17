package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupportTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id")
    private LoanAccount loanAccount;
    private String subject;
    private String description;
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
}
