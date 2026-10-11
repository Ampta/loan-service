package com.ampta.controller;

import com.ampta.dto.request.LoanReviewRequest;
import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.service.impl.LoanReviewServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/officer")
@RequiredArgsConstructor
public class LoanReviewController {

    private final LoanReviewServiceImpl loanReviewService;

    @PutMapping("/loan/{dealId}/decision")
    public ResponseEntity<LoanReviewResponse> reviewLoan(
            @PathVariable Long dealId,
            @RequestParam Long officerId,
            @RequestBody LoanReviewRequest request
            )
    {
        LoanReviewResponse response=loanReviewService.reviewLoan(
                dealId, officerId, request);

        return ResponseEntity.ok(response);
    }
}
