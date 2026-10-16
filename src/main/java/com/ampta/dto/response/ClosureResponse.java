package com.ampta.dto.response;

import com.ampta.entity.ClosureRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClosureResponse(
        Long requestId,
        Long loanAccountId,
        BigDecimal amount,
        String status,
        String rejectionReason,
        String paymentReference,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        LocalDateTime paidAt,
        LocalDateTime completedAt
) {

    public static ClosureResponse from(ClosureRequest e) {
        return new ClosureResponse(
                e.getRequestId(),
                e.getLoanAccount().getLoanAccountId(),
                e.getPaymentAmount(),
                e.getStatus(),
                e.getRejectionReason(),
                e.getPaymentReference(),
                e.getCreatedAt(),
                e.getReviewedAt(),
                e.getPaidAt(),
                e.getCompletedAt()
        );
    }
}