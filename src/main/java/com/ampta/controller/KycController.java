package com.ampta.controller;

import com.ampta.dto.request.BulkKycReviewRequest;
import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.KycDocumentResponse;
import com.ampta.dto.response.OfficerCustomerResponse;
import com.ampta.entity.enums.DocumentStatus;
import com.ampta.service.KycService;
import com.ampta.utils.Endpoints;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(Endpoints.V1_KYC)
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    // CUSTOMER-SIDE APIs

    // 1. Upload a KYC document
    @PostMapping(value = "/customers/{customerId}/documents",
    consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycDocumentResponse>> uploadDocument(@PathVariable Long customerId,
                                              @Valid @ModelAttribute KycDocumentRequest request) throws IOException{
        KycDocumentResponse response = kycService.uploadDocument(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Document uploaded successfully", response));

    }

    // 2. Get all documents uploaded by a customer

    @GetMapping("/customers/{customerId}/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>>
    getCustomerDocuments(@PathVariable Long customerId)
    {

        List<KycDocumentResponse> documents = kycService.getCustomerDocuments(customerId);

        return ResponseEntity.ok(new ApiResponse<>(true,"Documents fetched successfully", documents));
    }


    // 3. Get one particular customer document

    @GetMapping("/customers/{customerId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<KycDocumentResponse>>
    getCustomerDocument(
            @PathVariable Long customerId,
            @PathVariable Long documentId
    ) {

        KycDocumentResponse response =
                kycService.getCustomerDocument(
                        customerId,
                        documentId
                );

        return ResponseEntity.ok(new ApiResponse<>(true,  "Document fetched successfully", response)
        );
    }


    // 4. View a customer's uploaded document
    // GET /api/v1/kyc/customers/{userId}/documents/{documentId}/view

    @GetMapping(
            "/customers/{customerId}/documents/{documentId}/view"
    )
    public ResponseEntity<Resource> viewCustomerDocument(
            @PathVariable Long customerId,
            @PathVariable Long documentId
    ) {

        Resource resource =
                kycService.viewCustomerDocument(customerId, documentId);

        return buildFileResponse(resource);

    }


    // 5. Delete a customer's document
    // DELETE /api/v1/kyc/customers/{userId}/documents/{documentId}

    @DeleteMapping("/customers/{customerId}/documents/{documentId}"
    )
    public ResponseEntity<ApiResponse<Void>> deleteCustomerDocument(
            @PathVariable Long customerId,
            @PathVariable Long documentId
    ) {

        kycService.deleteCustomerDocument(
                customerId,
                documentId
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Document deleted successfully", null));
    }

 // LOAN OFFICER-SIDE APIs

    @GetMapping("/officer/customers")
    public ResponseEntity<ApiResponse<List<OfficerCustomerResponse>>>
    getCustomersWithPendingKyc() {

        List<OfficerCustomerResponse> customers =
                kycService.getCustomersWithPendingKyc();

        ApiResponse<List<OfficerCustomerResponse>> response =
                new ApiResponse<>(
                        true,
                        "Customers with pending KYC fetched successfully",
                        customers
                );

        return ResponseEntity.ok(response);
    }

    // Bulk approve/reject KYC documents
    @PutMapping("/officer/customers/{customerId}/documents/bulk-review")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> bulkReviewDocuments(
            @PathVariable Long customerId,
            @Valid @RequestBody BulkKycReviewRequest request) {

        List<KycDocumentResponse> reviewedDocuments =
                kycService.bulkReviewDocuments(customerId, request);

        String message;

        if (request.getStatus() == DocumentStatus.APPROVED) {
            message = "Selected KYC documents approved successfully";
        } else {
            message = "Selected KYC documents rejected successfully";
        }

        ApiResponse<List<KycDocumentResponse>> response =
                new ApiResponse<>(
                        true,
                        message,
                        reviewedDocuments
                );

        return ResponseEntity.ok(response);
    }

    // Officer - View Pending KYC Document

    @GetMapping("/officer/customers/{customerId}/documents/{documentId}/view")
    public ResponseEntity<Resource> viewOfficerKycDocument(
            @PathVariable Long customerId,
            @PathVariable Long documentId) {

        Resource resource =
                kycService.viewPendingDocumentForOfficer(
                        customerId,
                        documentId
                );

        MediaType mediaType =
                MediaTypeFactory.getMediaType(resource.getFilename())
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }



    // Officer - Get selected customer's pending KYC documents

    @GetMapping("/officer/customers/{customerId}/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>>
    getCustomerPendingDocumentsForOfficer(
            @PathVariable Long customerId) {

        List<KycDocumentResponse> documents =
                kycService.getCustomerPendingDocumentsForOfficer(customerId);

        ApiResponse<List<KycDocumentResponse>> response =
                new ApiResponse<>(
                        true,
                        "Pending KYC documents fetched successfully",
                        documents
                );

        return ResponseEntity.ok(response);
    }



    // COMMON FILE RESPONSE

    private ResponseEntity<Resource> buildFileResponse(
            Resource resource
    ) {

        MediaType mediaType =
                MediaTypeFactory.getMediaType(resource.getFilename()
                ).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + resource.getFilename()
                                + "\""
                )
                .body(resource);
    }
}