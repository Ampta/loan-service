package com.ampta.service;

import com.ampta.dto.request.BulkKycReviewRequest;
import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.request.KycReviewRequest;
import com.ampta.dto.response.KycDocumentResponse;
import com.ampta.dto.response.OfficerCustomerResponse;
import com.ampta.entity.Customer;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.util.List;

public interface KycService {

    // Customer uploads the document
    KycDocumentResponse uploadDocument(Long userId, KycDocumentRequest request) throws IOException;

    // Get all documents uploaded by customer
    List<KycDocumentResponse> getCustomerDocuments(
            Long customerId
    );

    // Get one particular document of customer
    KycDocumentResponse getCustomerDocument(
            Long customerId,
            Long documentId
    );


    // View customer's document
    Resource viewCustomerDocument(
            Long customerId,
            Long documentId
    );

    // Delete customer's document
    void deleteCustomerDocument(
            Long customerId,
            Long documentId
    );

    // Officer Side

    List<KycDocumentResponse> bulkReviewDocuments(
            Long customerId,
            BulkKycReviewRequest request
    );

    List<OfficerCustomerResponse> getCustomersWithPendingKyc();

    Resource viewPendingDocumentForOfficer(
            Long customerId,
            Long documentId
    );

    List<KycDocumentResponse> getCustomerPendingDocumentsForOfficer(
            Long customerId);

}