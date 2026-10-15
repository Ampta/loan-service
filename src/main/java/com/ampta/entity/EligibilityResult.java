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
public class EligibilityResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eligibility_id")
    private Long eligibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Column(name = "is_eligible", nullable = false)
    private Boolean isEligible = false;

    // Keep Double. No precision/scale attributes.
    @Column(
            name = "loan_amount",
            columnDefinition = "DECIMAL(18,2)"
    )
    private Double loanAmount;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
}