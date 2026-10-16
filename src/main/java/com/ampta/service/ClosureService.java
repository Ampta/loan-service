package com.ampta.service;

import com.ampta.dto.request.ClosureCreateRequest;
import com.ampta.dto.response.ClosureResponse;

import java.util.List;

public interface ClosureService {

    ClosureResponse create(ClosureCreateRequest input);

    ClosureResponse get(Long id);

    List<ClosureResponse> all();

    List<ClosureResponse> byLoan(Long loanId);

    ClosureResponse approve(Long id);

    ClosureResponse reject(Long id, String reason);

    ClosureResponse pay(Long id, String paymentReference);

    ClosureResponse complete(Long id);
}