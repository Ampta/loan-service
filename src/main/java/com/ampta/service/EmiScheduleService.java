package com.ampta.service;

import com.ampta.dto.request.EmiPaymentStatusRequest;
import com.ampta.dto.request.EmiScheduleRequest;
import com.ampta.dto.response.EmiScheduleResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmiScheduleService {
    List<EmiScheduleResponse> createEmiSchedule(EmiScheduleRequest request);
    Page<EmiScheduleResponse> getEmiSchedulesByLoanAccountId(Long loanAccountId, Pageable pageable);
    EmiScheduleResponse getEmiScheduleById(Long emiScheduleId);
    EmiScheduleResponse updatePaymentStatus(Long emiScheduleId, @Valid EmiPaymentStatusRequest request);
    List<EmiScheduleResponse> getEmiScheduleDetailsByLoanAccountId(Long loanAccountId);
}
