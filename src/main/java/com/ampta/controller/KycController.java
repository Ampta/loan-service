package com.ampta.controller;

import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.request.KycReviewRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.KycDocumentResponse;
import com.ampta.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;


    // =====================================================
    // CUSTOMER SIDE
    // =====================================================

    // Upload KYC document
    @PostMapping(
            value = "/customers/{userId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<KycDocumentResponse>> uploadDocument(
            @PathVariable Long userId,
            @Valid @ModelAttribute KycDocumentRequest request
    ) throws IOException {

        KycDocumentResponse response =
                kycService.uploadDocument(userId, request);

        return ResponseEntity.status(201).body(
                new ApiResponse<>(
                        true,
                        "Document uploaded successfully",
                        response
                )
        );
    }


    // Get all documents uploaded by customer
    @GetMapping("/customers/{userId}/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getCustomerDocuments(
            @PathVariable Long userId
    ) {

        List<KycDocumentResponse> documents =
                kycService.getCustomerDocuments(userId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Documents fetched successfully",
                        documents
                )
        );
    }


    // Get one particular document
    @GetMapping("/customers/{userId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> getCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        KycDocumentResponse response =
                kycService.getCustomerDocument(
                        userId,
                        documentId
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document fetched successfully",
                        response
                )
        );
    }


    // View customer's document
    @GetMapping("/customers/{userId}/documents/{documentId}/view")
    public ResponseEntity<Resource> viewCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        Resource resource =
                kycService.viewCustomerDocument(
                        userId,
                        documentId
                );

        return buildFileResponse(resource);
    }


    // Delete customer's document
    @DeleteMapping("/customers/{userId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId
    ) {

        kycService.deleteCustomerDocument(
                userId,
                documentId
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document deleted successfully",
                        null
                )
        );
    }


    // =====================================================
    // OFFICER SIDE
    // =====================================================

    // Get all pending documents
    @GetMapping("/officer/pending")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>>
    getDocumentsForReview() {

        List<KycDocumentResponse> documents =
                kycService.getDocumentsForReview();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Pending documents fetched successfully",
                        documents
                )
        );
    }


    // Get particular document for officer review
    @GetMapping("/officer/documents/{documentId}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>>
    getDocumentForReview(
            @PathVariable Long documentId
    ) {

        KycDocumentResponse response =
                kycService.getDocumentForReview(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document fetched successfully",
                        response
                )
        );
    }


    // View document by officer
    @GetMapping("/officer/documents/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable Long documentId
    ) {

        Resource resource =
                kycService.viewDocument(documentId);

        return buildFileResponse(resource);
    }


    // Approve / Reject document
    @PutMapping("/officer/documents/{documentId}/review")
    public ResponseEntity<ApiResponse<KycDocumentResponse>>
    reviewDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody KycReviewRequest request
    ) {

        KycDocumentResponse response =
                kycService.reviewDocument(
                        documentId,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document reviewed successfully",
                        response
                )
        );
    }


    // =====================================================
    // COMMON FILE RESPONSE
    // =====================================================

    private ResponseEntity<Resource> buildFileResponse(
            Resource resource
    ) {

        MediaType mediaType =
                MediaTypeFactory.getMediaType(
                        resource.getFilename()
                ).orElse(
                        MediaType.APPLICATION_OCTET_STREAM
                );

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }
}