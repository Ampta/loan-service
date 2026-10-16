package com.ampta.service;

import com.ampta.dto.request.ForeclosureCreateRequest;
import com.ampta.dto.response.ForeclosureResponse;

import java.util.List;

public interface ForeclosureService {

    ForeclosureResponse create(ForeclosureCreateRequest input);

    ForeclosureResponse get(Long id);

    List<ForeclosureResponse> all();

    List<ForeclosureResponse> byLoan(Long loanId);

    ForeclosureResponse approve(Long id);

    ForeclosureResponse reject(Long id, String reason);

    ForeclosureResponse pay(Long id, String paymentReference);

    ForeclosureResponse complete(Long id);
}