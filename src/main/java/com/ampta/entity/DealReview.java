
package com.ampta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deal_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "deal_id",
            referencedColumnName = "deal_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_DealReviews_LoanDeals")
    )
    private LoanDeal loanDeal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "officer_id",
            referencedColumnName = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_DealReviews_Customers")
    )
    private User officer;

    @Column(name = "status", length = 50)
    private String status;
}
