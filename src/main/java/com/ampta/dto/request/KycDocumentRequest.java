package com.ampta.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentRequest {

    @NotNull(message = "Document type is required")
    private String documentType;

    @NotNull(message = "File is required")
    private MultipartFile file;

}
