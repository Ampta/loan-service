package com.ampta.dto.request;

import com.ampta.entity.enums.DocumentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkKycReviewRequest {

    @NotEmpty(message = "Please select at least one document")
    private List<@NotNull Long> documentIds;

    @NotNull(message = "Review status is required")
    private DocumentStatus status;

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;
}