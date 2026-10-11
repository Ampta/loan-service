
package com.ampta.entity;

import com.ampta.entity.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "disbursements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_Disbursements_DealId",
                        columnNames = "deal_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disbursement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disbursement_id")
    private Long disbursementId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "FK_Disbursements_LoanDeal")
    )
    private LoanDeal loanDeal;

    @Column(name = "disburse_amount", nullable = false)
    private Double disburseAmount;

    @Column(name = "bank_partner", length = 200)
    private String bankPartner;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;
}
