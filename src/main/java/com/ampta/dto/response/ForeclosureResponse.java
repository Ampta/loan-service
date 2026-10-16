package com.ampta.dto.response;

import com.ampta.entity.ForeclosureRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ForeclosureResponse(
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

    public static ForeclosureResponse from(ForeclosureRequest e) {
        return new ForeclosureResponse(
                e.getRequestId(),
                e.getLoanAccount().getLoanAccountId(),
                e.getSettlementAmount(),
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