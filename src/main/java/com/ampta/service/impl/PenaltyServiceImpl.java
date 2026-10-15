package com.ampta.service.impl;

import com.ampta.dto.response.PenaltyChargeResponse;
import com.ampta.entity.EmiSchedule;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.EmiScheduleRepository;
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
    private final EmiScheduleRepository emiScheduleRepository;


    @Override
    public List<PenaltyChargeResponse> getEmiPenalties(Long loanAccountId, Long emiScheduleId) {

        // Fetch EMI schedule
        EmiSchedule schedule = emiScheduleRepository
                .findById(emiScheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "EMI schedule not found with ID: "
                                        + emiScheduleId
                        ));

        // Validate that this EMI belongs to the given loan account
        if (schedule.getLoanAccount() == null
                || !loanAccountId.equals(
                schedule.getLoanAccount().getLoanAccountId())) {

            throw new ResourceNotFoundException(
                    "EMI schedule " + emiScheduleId
                            + " does not belong to loan account "
                            + loanAccountId
            );
        }

        // Fetch penalties after validation
        return penaltyChargeRepository
                .findByEmiSchedule_EmiScheduleId(emiScheduleId)
                .stream()
                .map(penaltyCharge ->
                        modelMapper.map(
                                penaltyCharge,
                                PenaltyChargeResponse.class
                        ))
                .collect(Collectors.toList());
    }
}

