package com.ampta.service;

import com.ampta.dto.response.PenaltyChargeResponse;

import java.util.List;

public interface PenaltyService {
    public List<PenaltyChargeResponse> getEmiPenalties(Integer emiScheduleId);
}
