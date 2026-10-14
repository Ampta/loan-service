package com.ampta.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmiPaymentStatusRequest {
    @NotBlank(message = "Payment status is required")
    private String paymentStatus; // PAID, PENDING, OVERDUE, CANCELLED
    private String cancellationReason;
}
