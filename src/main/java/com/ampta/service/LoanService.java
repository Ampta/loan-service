package com.ampta.service;

import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.request.LoanApplicationResponse;

import java.util.List;


public interface LoanService {

    LoanApplicationResponse applyLoan(Long userId,LoanApplicationRequest request);

    LoanApplicationResponse getLoanById(Long dealId);

    List<LoanApplicationResponse> getAllLoans();

    void deleteLoan(Long dealId);

}
