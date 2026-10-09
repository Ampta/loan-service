package com.ampta.dto.response;

import com.ampta.entity.enums.CibilStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CibilScoreResponse {

    private Long customerId;

    private String customerName;

    private String panNumber;

    private Integer cibilScore;

    private CibilStatus cibilStatus;

    private Instant lastCheckedDate;
}