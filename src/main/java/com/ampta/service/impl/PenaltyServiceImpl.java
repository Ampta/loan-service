package com.ampta.service.impl;

import com.ampta.dto.response.PenaltyChargeResponse;
import com.ampta.repository.PenaltyChargeRepository;
import com.ampta.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PenaltyServiceImpl implements PenaltyService {
    private final PenaltyChargeRepository penaltyChargeRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PenaltyChargeResponse> getEmiPenalties(Integer emiScheduleId) {
        return penaltyChargeRepository.findByEmiSchedule_EmiScheduleId(emiScheduleId).stream()
                .map(penaltyCharge ->
                        modelMapper.map(penaltyCharge, PenaltyChargeResponse.class))
                .collect(Collectors.toList());
    }

}
