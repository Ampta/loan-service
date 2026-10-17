package com.ampta.service;

import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.DisbursementResponse;

import java.util.List;

public interface DisbursementService {

    DisbursementResponse disburseLoan(DisbursementRequest request);
    List<DisbursementResponse> getAllDisbursement();
}
