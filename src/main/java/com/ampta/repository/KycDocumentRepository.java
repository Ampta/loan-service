package com.ampta.repository;

import com.ampta.entity.Customer;
import com.ampta.entity.enums.DocumentStatus;
import com.ampta.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

//
//    // Customer side - get all documents of a user
//    List<KycDocument> findByUserUserId(Long userId);
//
//    // Customer side - get one particular document
//    Optional<KycDocument> findByDocumentIdAndCustomerCustomerId(
//            Long documentId,
//            Long customerId
//    );
//
//    // Officer side - get pending documents
//    List<KycDocument> findByVerificationStatus(
//            DocumentStatus verificationStatus
//    );

    List<KycDocument> findByCustomer_CustomerId(Long customerId);
// Get all documents belonging to a customer
List<KycDocument> findByCustomer_CustomerIdOrderByDocumentIdDesc(
        Long customerId
);

    // Get a particular document belonging to a customer
    Optional<KycDocument> findByDocumentIdAndCustomer_CustomerId(
            Long documentId,
            Long customerId
    );

    // Get documents by verification status
    List<KycDocument> findByVerificationStatus(
            DocumentStatus verificationStatus
    );

    List<KycDocument> findByCustomer_CustomerIdAndVerificationStatus(
            Long customerId,
            DocumentStatus verificationStatus
    );
    @Query("""
       SELECT DISTINCT k.customer
       FROM KycDocument k
       WHERE k.verificationStatus = :status
       """)
    List<Customer> findCustomersWithDocumentStatus(
            @Param("status") DocumentStatus status
    );

}
