package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eligibility_results")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EligibilityResult extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eligibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private Integer cibilScore;

    @Column(nullable = false)
    private Boolean isEligible = false;

    private Double loanAmount;

    private String rejectionReason;
}
