
package com.ampta.entity;

import com.ampta.entity.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "sanction_letters",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_SanctionLetters_LetterNo",
                        columnNames = "sanction_letter_no"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sanction_id")
    private Integer sanctionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "FK_SanctionLetters_LoanDeals")
    )
    private LoanDeal loanDeal;


    @Column(name = "sanction_letter_no", nullable = false, length = 50)
    private String sanctionLetterNo;

    @Column(name = "sanctioned_amount", nullable = false)
    private Double sanctionedAmount;

    @Column(name = "interest_rate", nullable = false)
    private Double interestRate;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "emi_amount")
    private Double emiAmount;

    @Column(name = "loan_amount", precision = 18, scale = 2)
    private java.math.BigDecimal loanAmount;

    @Column(name = "sanction_date", nullable = false)
    private LocalDate sanctionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LoanStatus status;

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @Column(name = "letter_content", columnDefinition = "TEXT")
    private String letterContent;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
