package com.ampta.dto.response;

import com.ampta.entity.enums.CibilStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CibilScoreResponse {

    private Long customerId;

    private String customerName;

    private String panNumber;

    private String cibilScore;

    private CibilStatus cibilStatus;

    private Instant lastCheckedDate;
}