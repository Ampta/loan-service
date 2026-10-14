package com.ampta.service;

import com.ampta.dto.response.PenaltyChargeResponse;

import java.util.List;

public interface PenaltyService {
    List<PenaltyChargeResponse> getEmiPenalties(
            Long loanAccountId,
            Long emiScheduleId
    );
}
