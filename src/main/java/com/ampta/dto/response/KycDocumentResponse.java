package com.ampta.dto.response;


import com.ampta.entity.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocumentResponse {

    private Long documentId;
    private Long customerId;
    private String documentType;
    private String filePath;
    private DocumentStatus verificationStatus;

}
