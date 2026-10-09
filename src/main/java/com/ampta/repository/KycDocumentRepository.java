package com.ampta.repository;

import com.ampta.entity.enums.DocumentStatus;
import com.ampta.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {


    // Customer side - get all documents of a user
    List<KycDocument> findByUserUserId(Long userId);

    // Customer side - get one particular document
    Optional<KycDocument> findByDocumentIdAndUserUserId(
            Long documentId,
            Long userId
    );

    // Officer side - get pending documents
    List<KycDocument> findByVerificationStatus(
            DocumentStatus verificationStatus
    );
}
