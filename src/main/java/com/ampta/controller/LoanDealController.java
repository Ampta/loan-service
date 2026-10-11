package com.ampta.controller;

import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.request.LoanApplicationResponse;
import com.ampta.dto.response.ApiResponse;
import com.ampta.entity.LoanDeal;
import com.ampta.service.impl.LoanServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanDealController {

    private final LoanServiceImpl loanService;

    @PostMapping("/loan/{userId}")
    public ResponseEntity<LoanApplicationResponse> applyLoan(@PathVariable Long userId, @RequestBody LoanApplicationRequest
                                             request)
    {
        LoanApplicationResponse response=loanService.applyLoan(userId,request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan/{dealId}")
    public ResponseEntity<LoanApplicationResponse> getLoanById(@PathVariable Long dealId)
    {
        LoanApplicationResponse loanDeal=loanService.getLoanById(dealId);

        return ResponseEntity.ok(loanDeal);

    }

    @GetMapping("/loan")
    public ResponseEntity<List<LoanApplicationResponse>> getAllLoans()
    {
        List<LoanApplicationResponse> response=loanService.getAllLoans();

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("{dealId}")
    public ResponseEntity<String> deleteLoan(@PathVariable Long dealId)
    {
        loanService.deleteLoan(dealId);

        return ResponseEntity.ok("Loan Delete Successfully with id :"+ dealId);
    }

}
