package com.ampta.service.impl;

import com.ampta.config.ModelMapperConfig;
import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.request.KycReviewRequest;
import com.ampta.dto.response.KycDocumentResponse;
import com.ampta.entity.*;
import com.ampta.entity.enums.DocumentStatus;
import com.ampta.entity.enums.NotificationType;
import com.ampta.entity.enums.Role;
import com.ampta.repository.KycDocumentRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.KycService;
import com.ampta.service.NotificationService;
import com.ampta.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycServiceImpl implements KycService {

//    private final ModelMapper modelMapper;
    private final ModelMapperConfig modelMapperConfig;
    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file");
        }

        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
    }

    // =========================
    // Customer - Upload
    // =========================

    @Override
    public KycDocumentResponse uploadDocument(
            Long userId,KycDocumentRequest request) throws IOException {

        MultipartFile file = request.getFile();

        log.info("KYC document upload started, documentType={}",
                request.getDocumentType());

        validateFile(file);

        // Create upload directory
        Path uploadDir = Paths.get(
                System.getProperty("user.dir"),
                "uploads",
                "kyc"
        );

        Files.createDirectories(uploadDir);

// Generate unique file name
        String fileName =
                UUID.randomUUID() + "_" + "PAN" + file.getOriginalFilename() ;

// Complete file path
        Path filePath = uploadDir.resolve(fileName);

// Save physical file
        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        log.debug("KYC file saved successfully, fileName={}", fileName);

        // Temporary user for testing
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Request DTO -> Entity
        KycDocument document = modelMapperConfig.modelMapper().map(request, KycDocument.class);

        // Fields that need manual mapping
        document.setUser(user);
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(filePath.toString());
        document.setVerificationStatus(DocumentStatus.PENDING);

        // Save entity
        KycDocument savedDocument = kycDocumentRepository.save(document);

        log.info(
                "KYC document saved successfully, documentId={}, userId={}, status={}",
                savedDocument.getDocumentId(),
                user.getUserId(),
                savedDocument.getVerificationStatus()
        );

//        Notification to officer
        User officer = userRepository.findByRole(Role.OFFICER)
                .orElseThrow(() ->
                        new RuntimeException("Officer not found"));

        notificationService.sendNotification(
                officer.getUserId(),
                NotificationType.KYC_DOCUMENT_UPLOADED,
                "New KYC Document",
                "A new " + savedDocument.getDocumentType()
                        + " document is waiting for verification.",
                savedDocument.getDocumentId()
        );

        log.info(
                "KYC upload notification sent, officerId={}, documentId={}",
                officer.getUserId(),
                savedDocument.getDocumentId()
        );
// -----------------------------------------------
        // Entity -> Response DTO
        KycDocumentResponse response = modelMapperConfig.modelMapper().map(savedDocument, KycDocumentResponse.class);

        response.setCustomerId(savedDocument.getUser().getUserId());

        return response;
    }

    // =========================
    // Customer - Get All
    // =========================

    @Override
    public List<KycDocumentResponse> getCustomerDocuments(Long userId) {

        List<KycDocument> documents = kycDocumentRepository.findByUserUserId(userId);

        return documents.stream().map(document -> {
            KycDocumentResponse response = modelMapperConfig.modelMapper().map(
                                    document,
                                    KycDocumentResponse.class
                            );

                    response.setCustomerId(
                            document.getUser().getUserId()
                    );

                    return response;
                })
                .collect(Collectors.toList());
    }

    // =========================
    // Customer - Get One
    // =========================

    @Override
    public KycDocumentResponse getCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository
                        .findByDocumentIdAndUserUserId(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "KYC document not found"
                                ));

        KycDocumentResponse response =
                modelMapperConfig.modelMapper().map(
                        document,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                document.getUser().getUserId()
        );

        return response;
    }

    // =========================
    // Customer - View
    // =========================

    @Override
    public Resource viewCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository
                        .findByDocumentIdAndUserUserId(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "KYC document not found"
                                ));

        if (document.getFilePath() == null) {
            throw new RuntimeException("File path not found");
        }

        Path path = Paths.get(document.getFilePath());

        if (!Files.exists(path)) {
            throw new RuntimeException("Document file not found");
        }

        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Unable to load document",
                    e
            );
        }
    }

    // =========================
    // Customer - Delete
    // =========================

    @Override
    public void deleteCustomerDocument(
            Long userId,
            Long documentId) {

        KycDocument document =
                kycDocumentRepository
                        .findByDocumentIdAndUserUserId(
                                documentId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "KYC document not found"
                                ));

        // Delete physical file
        if (document.getFilePath() != null) {

            Path path =
                    Paths.get(document.getFilePath());

            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Unable to delete KYC file",
                        e
                );
            }
        }

        // Delete DB record
        kycDocumentRepository.delete(document);
    }

    @Override
    public Resource viewDocument(Long documentId) {

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("KYC document not found"));

        Path path = Paths.get(document.getFilePath());

        if (!Files.exists(path)) {
            throw new RuntimeException("Document file not found");
        }

        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to load document", e);
        }
    }

//    get all pending kyc documents
@Override
public List<KycDocumentResponse> getDocumentsForReview() {

    List<KycDocument> documents =
            kycDocumentRepository.findByVerificationStatus(
                    DocumentStatus.PENDING
            );

    return documents.stream()
            .map(document -> {

                KycDocumentResponse response =
                        modelMapperConfig.modelMapper().map(
                                document,
                                KycDocumentResponse.class
                        );

                response.setCustomerId(
                        document.getUser().getUserId()
                );

                return response;
            })
            .collect(Collectors.toList());
    }

//Get particular document for officer
    @Override
    public KycDocumentResponse getDocumentForReview(Long documentId) {

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "KYC document not found"
                                ));

        KycDocumentResponse response =
                modelMapperConfig.modelMapper().map(
                        document,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                document.getUser().getUserId()
        );

        return response;
    }
//Approve / Reject document
    @Override
    public KycDocumentResponse reviewDocument(
            Long documentId,
            KycReviewRequest request) {

        log.info(
                "KYC document review started, documentId={}, requestedStatus={}",
                documentId,
                request.getStatus()
        );

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "KYC document not found"
                                ));

        // Already approved documents cannot be changed
        if (document.getVerificationStatus() == DocumentStatus.APPROVED) {

            log.warn(
                    "Attempt to modify already approved KYC, documentId={}",
                    documentId
            );

            throw new RuntimeException(
                    "Approved KYC document cannot be changed"
            );
        }

        // Reject reason validation
        if (request.getStatus() == DocumentStatus.REJECTED
                && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {

            log.warn(
                    "KYC rejection attempted without reason, documentId={}",
                    documentId
            );

            throw new RuntimeException(
                    "Rejection reason is required"
            );
        }

        DocumentStatus oldStatus =
                document.getVerificationStatus();

        // Update verification status
        document.setVerificationStatus(request.getStatus());

        // Save updated document
        KycDocument updatedDocument =
                kycDocumentRepository.save(document);

        log.info(
                "KYC document status updated, documentId={}, oldStatus={}, newStatus={}",
                documentId,
                oldStatus,
                updatedDocument.getVerificationStatus()
        );

//        Notification to customer (approve/reject)


        User customer = updatedDocument.getUser();

        // APPROVED
        if (request.getStatus() == DocumentStatus.APPROVED) {

            notificationService.sendNotification(
                    customer.getUserId(),
                    NotificationType.KYC_DOCUMENT_APPROVED,
                    "KYC Document Approved",
                    "Your " + updatedDocument.getDocumentType()
                            + " document has been approved.",
                    updatedDocument.getDocumentId()
            );
            emailService.sendKycStatusEmail(
                    customer.getEmail(),
                    customer.getFirstName(),
                    updatedDocument.getDocumentType(),
                    DocumentStatus.APPROVED,
                    null
            );

            log.info(
                    "KYC approval notification and email triggered, documentId={}, customerId={}",
                    documentId,
                    customer.getUserId()
            );
        }

        // REJECTED
        if (request.getStatus() == DocumentStatus.REJECTED) {

            String message =
                    "Your " + updatedDocument.getDocumentType()
                            + " document has been rejected.";

            if (request.getRejectionReason() != null
                    && !request.getRejectionReason().isBlank()) {

                message += " Reason: "
                        + request.getRejectionReason();
            }

            notificationService.sendNotification(
                    customer.getUserId(),
                    NotificationType.KYC_DOCUMENT_REJECTED,
                    "KYC Document Rejected",
                    message,
                    updatedDocument.getDocumentId()
            );
            emailService.sendKycStatusEmail(
                    customer.getEmail(),
                    customer.getFirstName(),
                    updatedDocument.getDocumentType(),
                    DocumentStatus.REJECTED,
                    request.getRejectionReason()
            );

            log.info(
                    "KYC rejection notification and email triggered, documentId={}, customerId={}",
                    documentId,
                    customer.getUserId()
            );

        }


//        ---------------------------------------
        // Entity -> Response DTO
        KycDocumentResponse response =
                modelMapperConfig.modelMapper().map(
                        updatedDocument,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                updatedDocument.getUser().getUserId()
        );

        return response;
    }
}