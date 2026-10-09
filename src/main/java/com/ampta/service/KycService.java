package com.ampta.service;

import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.request.KycReviewRequest;
import com.ampta.dto.response.KycDocumentResponse;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.util.List;

public interface KycService {

    // Customer
    KycDocumentResponse uploadDocument(Long userId, KycDocumentRequest request) throws IOException;
    // Get all documents uploaded by customer
    List<KycDocumentResponse> getCustomerDocuments(
            Long userId
    );

    // Get one particular document of customer
    KycDocumentResponse getCustomerDocument(
            Long userId,
            Long documentId
    );

//    // Download customer's document
//    Resource downloadCustomerDocument(
//            Long userId,
//            Long documentId
//    );

    // View customer's document
    Resource viewCustomerDocument(
            Long userId,
            Long documentId
    );

    // Delete customer's document
    void deleteCustomerDocument(
            Long userId,
            Long documentId
    );


    // =========================
    // Officer Side
    // =========================

    // Get all documents which are pending for review
    List<KycDocumentResponse> getDocumentsForReview();

    // Get particular document for officer review
    KycDocumentResponse getDocumentForReview(
            Long documentId
    );

    // View document
    Resource viewDocument(
            Long documentId
    );

    // Approve / Reject document
    KycDocumentResponse reviewDocument(
            Long documentId,
            KycReviewRequest request
    );
}