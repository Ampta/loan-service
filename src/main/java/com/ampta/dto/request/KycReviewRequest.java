package com.ampta.dto.request;
import com.ampta.entity.enums.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycReviewRequest {

    @NotNull
    private DocumentStatus status;

    private String rejectionReason;

}
