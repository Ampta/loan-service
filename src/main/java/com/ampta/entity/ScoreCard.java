package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "score_cards")
public class ScoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreCardId;
    private Long userId;
    private String status;
    private String rejectionReason;
    private Integer cibilScore;
    private String riskCategory;
    private Double eligibleLoanAmount;

}
