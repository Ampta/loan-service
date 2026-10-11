package com.ampta.service.impl;

import com.ampta.dto.request.BulkKycReviewRequest;
import com.ampta.dto.response.OfficerCustomerResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.KycDocument;
import com.ampta.entity.enums.DocumentStatus;
import com.ampta.exception.KycDocumentAlreadyReviewedException;
import com.ampta.exception.RejectionReasonRequiredException;
import com.ampta.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import com.ampta.config.ModelMapperConfig;

import com.ampta.dto.request.KycDocumentRequest;
import com.ampta.dto.response.KycDocumentResponse;

import com.ampta.entity.User;

import com.ampta.entity.enums.NotificationType;
import com.ampta.entity.enums.Role;
import com.ampta.exception.FileStorageException;
import com.ampta.exception.KycDocumentCannotBeDeletedException;

import com.ampta.repository.CustomerRepository;
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
    private final CustomerRepository customerRepository;

    // Customer - Upload

    @Override
    public KycDocumentResponse uploadDocument(
            Long customerId, KycDocumentRequest request) throws IOException {

        MultipartFile file = request.getFile();

        log.info("KYC document upload started, documentType={}",
                request.getDocumentType());
//validating file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file");
        }

        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Create upload directory
        Path uploadDir = Paths.get(
                System.getProperty("user.dir"),
                "uploads",
                "kyc"
        );

        Files.createDirectories(uploadDir);

// Generate unique file name
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

// Complete file path
        Path filePath = uploadDir.resolve(fileName);

        try {
            Files.createDirectories(uploadDir);

            try (InputStream inputStream = file.getInputStream()) {
                // Save physical file

                Files.copy(
                        inputStream,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException ex) {

            log.error("Failed to store KYC document", ex);

            throw new FileStorageException(
                    "Unable to store KYC document",
                    ex
            );
        }

        log.debug("KYC file saved successfully, fileName={}", fileName);

        // Temporary user for testing
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found with id: " + customerId));

        // Request DTO -> Entity
        KycDocument document = modelMapperConfig.modelMapper().map(request, KycDocument.class);

        // Fields that need manual mapping
        document.setCustomer(customer);
        document.setDocumentType(request.getDocumentType());
        document.setFilePath(filePath.toString());
        document.setVerificationStatus(DocumentStatus.PENDING);

        // Save entity
        KycDocument savedDocument = kycDocumentRepository.save(document);

        log.info(
                "KYC document saved successfully, documentId={}, userId={}, status={}",
                savedDocument.getDocumentId(),
                customer.getCustomerId(),
                savedDocument.getVerificationStatus()
        );

//        Notification to officer
            User officer = userRepository.findByRole(Role.OFFICER)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Loan officer not found"
                            ));

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

        // Entity -> Response DTO
        KycDocumentResponse response = modelMapperConfig.modelMapper().map(savedDocument, KycDocumentResponse.class);

        response.setCustomerId(savedDocument.getCustomer().getCustomerId());

        return response;
    }

    // Customer - Get All
//PENDING, APPROVED aur REJECTED
    @Override
    public List<KycDocumentResponse> getCustomerDocuments(Long customerId) {

        // Step 1: Check whether customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        // Step 2: Fetch customer's documents
        List<KycDocument> documents = kycDocumentRepository.findByCustomer_CustomerId(customerId);

        return documents.stream().map(document -> {
            KycDocumentResponse response = modelMapperConfig.modelMapper().map(
                                    document,
                                    KycDocumentResponse.class
                            );

                    response.setCustomerId(
                            document.getCustomer().getCustomerId()
                    );

                    return response;
                }).collect(Collectors.toList());
    }

    // Customer - Get One (customer list record)
    @Override
    public KycDocumentResponse getCustomerDocument(
            Long customerId,
            Long documentId) {

        // Step 1: Check whether customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        KycDocument document = kycDocumentRepository
                        .findByDocumentIdAndCustomer_CustomerId(
                                documentId,
                                customerId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));

        KycDocumentResponse response =
                modelMapperConfig.modelMapper().map(
                        document,
                        KycDocumentResponse.class
                );

        response.setCustomerId(
                document.getCustomer().getCustomerId()
        );

        return response;
    }

    // Customer - View -> customer can view uploaded document
    @Override
    @Transactional(readOnly = true)
    public Resource viewCustomerDocument(
            Long customerId,
            Long documentId) {

        // Step 1: Check customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        ));

        // Step 2: Find document belonging to this customer
        KycDocument document = kycDocumentRepository
                .findByDocumentIdAndCustomer_CustomerId(
                        documentId,
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found"
                        ));

        // Step 3: Validate file path
        String filePath = document.getFilePath();

        if (filePath == null || filePath.isBlank()) {
            throw new FileStorageException(
                    "Document file path is missing"
            );
        }

        // Step 4: Resolve the actual file path
        Path uploadDir = Paths.get(
                System.getProperty("user.dir"),
                "uploads",
                "kyc"
        ).toAbsolutePath().normalize();

        Path path = Paths.get(filePath)
                .toAbsolutePath()
                .normalize();

        // Step 5: Ensure the file is inside KYC storage
        if (!path.startsWith(uploadDir)) {
            throw new FileStorageException(
                    "Invalid document storage path"
            );
        }

        // Step 6: Check file availability
        if (!Files.isRegularFile(path)
                || !Files.isReadable(path)) {
            throw new FileStorageException(
                    "Document file is not available"
            );
        }

        // Step 7: Return file as a Resource
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException ex) {
            throw new FileStorageException(
                    "Unable to load document",
                    ex
            );
        }
    }

    // Customer - Delete -> customer can delete their uploaded doc
    @Override
    @Transactional
    public void deleteCustomerDocument(
            Long customerId,
            Long documentId) {

        log.info(
                "KYC document deletion started, customerId={}, documentId={}",
                customerId,
                documentId
        );

        // Step 1: Check whether customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        // Step 2: Find the document belonging to this customer
        KycDocument document = kycDocumentRepository
                .findByDocumentIdAndCustomer_CustomerId(
                        documentId,
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found"
                        )
                );

        // Step 3: Only PENDING documents can be deleted
        if (document.getVerificationStatus() != DocumentStatus.PENDING) {
            throw new KycDocumentCannotBeDeletedException(
                    "Only pending KYC documents can be deleted"
            );
        }

        // Step 4: Delete the physical file, if its path is present
        String filePath = document.getFilePath();

        if (filePath != null && !filePath.isBlank()) {

            Path uploadDir = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "kyc"
            ).toAbsolutePath().normalize();

            Path path = Paths.get(filePath)
                    .toAbsolutePath()
                    .normalize();

            // Prevent deletion outside the KYC upload directory
            if (!path.startsWith(uploadDir)) {
                throw new FileStorageException(
                        "Invalid document storage path"
                );
            }

            try {
                Files.deleteIfExists(path);

            } catch (IOException ex) {

                log.error(
                        "Failed to delete KYC file, documentId={}",
                        documentId,
                        ex
                );

                throw new FileStorageException(
                        "Unable to delete KYC file",
                        ex
                );
            }
        }

        // Step 5: Delete the database record
        kycDocumentRepository.delete(document);

        log.info(
                "KYC document deleted successfully, customerId={}, documentId={}",
                customerId,
                documentId
        );
    }


    // Get pending KYC documents of a particular customer
    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getCustomerPendingDocumentsForOfficer(
            Long customerId) {

        // Step 1: Check whether customer exists
        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        // Step 2: Fetch only PENDING documents of this customer
        List<KycDocument> documents =
                kycDocumentRepository
                        .findByCustomer_CustomerIdAndVerificationStatus(
                                customerId,
                                DocumentStatus.PENDING
                        );

        // Step 3: Convert Entity -> Response DTO
        return documents.stream()
                .map(document -> {

                    KycDocumentResponse response =
                            modelMapperConfig.modelMapper().map(
                                    document,
                                    KycDocumentResponse.class
                            );

                    response.setCustomerId(
                            document.getCustomer().getCustomerId()
                    );

                    return response;
                })
                .toList();
    }
    @Override
    @Transactional
    public List<KycDocumentResponse> bulkReviewDocuments(
            Long customerId,
            BulkKycReviewRequest request) {

        // 1. Customer check
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + customerId
                        ));

        // Status validation (empty field will not get rejected)
        if (request == null
                || (request.getStatus() != DocumentStatus.APPROVED
                && request.getStatus() != DocumentStatus.REJECTED)) {

            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED"
            );
        }

// Document IDs validation
        if (request.getDocumentIds() == null
                || request.getDocumentIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select at least one document"
            );
        }

        // 2. Reason is mandatory for rejection
        if (request.getStatus() == DocumentStatus.REJECTED
                && (request.getRejectionReason() == null
                || request.getRejectionReason().isBlank())) {

            throw new RejectionReasonRequiredException(
                    "Rejection reason is required"
            );
        }

        // 3. Fetch selected documents
        List<KycDocument> documents =
                kycDocumentRepository.findAllById(
                        request.getDocumentIds()
                );

        // Check whether all selected documents exist
        if (documents.size() != request.getDocumentIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more documents not found"
            );
        }

        // 4. Ownership and PENDING status
        for (KycDocument document : documents) {

            if (!document.getCustomer().getCustomerId()
                    .equals(customerId)) {

                throw new ResourceNotFoundException(
                        "Document does not belong to this customer"
                );
            }

            if (document.getVerificationStatus()
                    != DocumentStatus.PENDING) {

                throw new KycDocumentAlreadyReviewedException(
                        "Document is already reviewed: "
                                + document.getDocumentId()
                );
            }
        }

        // 5. Documents approve/reject
        for (KycDocument document : documents) {

            document.setVerificationStatus(request.getStatus());

            if (request.getStatus() == DocumentStatus.REJECTED) {
                document.setRejectionReason(
                        request.getRejectionReason().trim()
                );
            } else {
                document.setRejectionReason(null);
            }
        }

        // 6. save updated documents database
        List<KycDocument> savedDocuments =
                kycDocumentRepository.saveAll(documents);

        // 7. Response DTO
        List<KycDocumentResponse> responses = new ArrayList<>();

        for (KycDocument document : savedDocuments) {

            KycDocumentResponse response =
                    modelMapperConfig.modelMapper()
                            .map(document, KycDocumentResponse.class);

            response.setCustomerId(customerId);

            responses.add(response);
        }

        // 8. send email and notification to the customer
        User customerUser = userRepository
                .findByCustomer_CustomerId(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User account not found for customer"
                        ));

        List<String> documentTypes = new ArrayList<>();

        for (KycDocument document : savedDocuments) {
            if (!documentTypes.contains(document.getDocumentType())) {
                documentTypes.add(document.getDocumentType());
            }
        }

        String message = "Your KYC documents "
                + String.join(", ", documentTypes)
                + " have been "
                + request.getStatus().name().toLowerCase()
                + ".";

        if (request.getStatus() == DocumentStatus.REJECTED) {
            message += " Reason: " + request.getRejectionReason();
        }

        NotificationType notificationType =
                request.getStatus() == DocumentStatus.APPROVED
                        ? NotificationType.KYC_DOCUMENT_APPROVED
                        : NotificationType.KYC_DOCUMENT_REJECTED;

        notificationService.sendNotification(
                customerUser.getUserId(),
                notificationType,
                "KYC Documents Reviewed",
                message,
                savedDocuments.get(0).getDocumentId()
        );

        emailService.sendBulkKycStatusEmail(
                customer.getEmail(),
                customer.getFirstName(),
                documentTypes,
                request.getStatus(),
                request.getRejectionReason()
        );

        return responses;
    }


    @Override
    public List<OfficerCustomerResponse> getCustomersWithPendingKyc() {

        List<Customer> customers =
                kycDocumentRepository.findCustomersWithDocumentStatus(
                        DocumentStatus.PENDING
                );

        List<OfficerCustomerResponse> responseList = new ArrayList<>();

        for (Customer customer : customers) {

            OfficerCustomerResponse response =
                    new OfficerCustomerResponse();

            response.setCustomerId(customer.getCustomerId());

            response.setCustomerName(
                    customer.getFirstName() + " " + customer.getLastName()
            );

            response.setEmail(customer.getEmail());
            response.setPhoneNumber(customer.getPhoneNumber());

            responseList.add(response);
        }

        return responseList;
    }

    // Officer - View Pending Document
    @Override
    @Transactional(readOnly = true)
    public Resource viewPendingDocumentForOfficer(
            Long customerId,
            Long documentId) {

        // Step 1: Find customer selected document
        KycDocument document = kycDocumentRepository
                        .findByDocumentIdAndCustomer_CustomerId(
                                documentId,
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "KYC document not found"
                                ));

        // Step 2: Check if document is PENDING
        if (document.getVerificationStatus() != DocumentStatus.PENDING) {
            throw new KycDocumentAlreadyReviewedException(
                    "This document has already been reviewed"
            );
        }

        // Step 3: view document
        return viewCustomerDocument(customerId, documentId);
    }

}