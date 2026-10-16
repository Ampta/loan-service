package com.ampta.controller;

import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.LoanApplicationResponse;
import com.ampta.entity.LoanDeal;
import com.ampta.service.impl.LoanServiceImpl;
import com.ampta.utils.Endpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoints.V1_LOAN)
@RequiredArgsConstructor
@CrossOrigin("*")
public class LoanDealController {

    private final LoanServiceImpl loanService;

    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> applyLoan(@PathVariable Long customerId,
                                                             @RequestBody LoanApplicationRequest
                                             request)
    {
        LoanApplicationResponse response=loanService.applyLoan(customerId,request);

        ApiResponse<LoanApplicationResponse> apiResponse=ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .data(response)
                .message("Your Successfully apply for the loan")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getLoanById(@PathVariable Long dealId)
    {
        LoanApplicationResponse loanDeal=loanService.getLoanById(dealId);

        ApiResponse<LoanApplicationResponse> apiResponse=ApiResponse.<LoanApplicationResponse>builder()
                .success(true)
                .data(loanDeal)
                .message("Loan Fetch Successfully")
                .build();


        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getAllLoans()
    {
        List<LoanApplicationResponse> response=loanService.getAllLoans();

        ApiResponse<List<LoanApplicationResponse>> apiResponse=ApiResponse.<List<LoanApplicationResponse>>builder()
                .success(true)
                .data(response)
                .message("Fetch All Apply Loans")
                .build();


        return ResponseEntity.ok(apiResponse);

    }

    @DeleteMapping("{dealId}")
    public ResponseEntity<ApiResponse<?>> deleteLoan(@PathVariable Long dealId)
    {
        loanService.deleteLoan(dealId);

        ApiResponse<String> apiResponse=ApiResponse.<String>builder()
                .success(true)
                .data(null)
                .message("Loan Delete Successfully with id :"+ dealId)
                .build();


        return ResponseEntity.ok(apiResponse);
    }

}
