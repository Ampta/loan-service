package com.ampta.service;

import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.DisbursementResponse;

public interface DisbursementService {

    DisbursementResponse disburseLoan(DisbursementRequest request);
}
