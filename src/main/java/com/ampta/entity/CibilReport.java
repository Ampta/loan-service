package com.ampta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
public class CibilReport extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "CibilReportId")
    private Long cibilReportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

//    @Column(length = 20)
    private String panNumber;

//    @Column(name = "CibilScore")
    private Integer cibilScore;

//    @Column(name = "CheckDate")
    private Instant checkDate;
}