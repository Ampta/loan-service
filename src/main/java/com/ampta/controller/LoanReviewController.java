package com.ampta.controller;

import com.ampta.dto.request.LoanReviewRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.service.impl.LoanReviewServiceImpl;
import com.ampta.utils.Endpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Endpoints.V1_OFFICER)
@RequiredArgsConstructor
public class LoanReviewController {

    private final LoanReviewServiceImpl loanReviewService;

    @PutMapping("/loan/{dealId}/decision")
    public ResponseEntity<ApiResponse<LoanReviewResponse>> reviewLoan(
            @PathVariable Long dealId,
            @RequestParam Long officerId,
            @RequestBody LoanReviewRequest request
            )
    {
        LoanReviewResponse dealReview=loanReviewService.reviewLoan(
                dealId, officerId, request);

        ApiResponse<LoanReviewResponse> apiResponse=ApiResponse.<LoanReviewResponse>builder()
                .success(true)
                .data(dealReview)
                .message("Loan Approved Successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
