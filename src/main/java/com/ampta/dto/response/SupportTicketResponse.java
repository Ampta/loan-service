package com.ampta.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupportTicketResponse {
    private Long ticketId;
    private Long customerId;
    private String customerName;
    private Long loanAccountId;
    private String subject;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
