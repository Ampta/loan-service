
package com.ampta.service.impl;

import com.ampta.config.SanctionLetterMapper;
import com.ampta.dto.response.SanctionLetterResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.SanctionLetter;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.LoanDealRepository;
import com.ampta.repository.SanctionLetterRepository;
import com.ampta.service.SanctionLetterService;
import com.ampta.utils.EmailServiceImpl;
import com.ampta.utils.SanctionLetterPdfGenerator;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SanctionLetterServiceImpl implements SanctionLetterService {

    private final LoanDealRepository loanDealRepository;
    private final SanctionLetterRepository sanctionLetterRepository;
    private final SanctionLetterMapper sanctionLetterMapper;
    private final SanctionLetterPdfGenerator pdfGenerator;
    private final CustomerRepository customerRepository;
    private final EmailServiceImpl emailService;

    // Generate sanction letter, PDF and send email
    @Override
    @Transactional
    public SanctionLetterResponse generateSanctionLetter(Long dealId) {

        // 1. Find loan deal
        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan deal not found: " + dealId
                        )
                );

        // 2. Validate loan status
        if (loanDeal.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "Sanction letter can only be generated for approved loans"
            );
        }

        // 3. Check duplicate sanction letter
        if (sanctionLetterRepository.existsByLoanDeal_DealId(dealId)) {
            throw new IllegalStateException(
                    "Sanction letter already exists for deal ID: " + dealId
            );
        }

        // 4. Validate approved amount
        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount() <= 0) {
            throw new IllegalStateException(
                    "Approved amount must be set before generating the sanction letter"
            );
        }

        // 5. Validate customer association
        if (loanDeal.getCustomer() == null
                || loanDeal.getCustomer().getCustomerId() == null) {
            throw new IllegalStateException(
                    "Customer is not associated with loan deal ID: " + dealId
            );
        }

        Long customerId = loanDeal.getCustomer().getCustomerId();

        // 6. Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Customer not found with ID: " + customerId
                        )
                );

        // 7. Validate customer email
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalStateException(
                    "Customer email is missing for customer ID: " + customerId
            );
        }

        // 8. Generate sanction letter number
        LocalDate today = LocalDate.now();
        String letterNumber = "SL-" + today.getYear() + "-" + dealId;

        // 9. Create sanction letter
        SanctionLetter sanctionLetter = SanctionLetter.builder()
                .loanDeal(loanDeal)
                .sanctionLetterNo(letterNumber)
                .sanctionedAmount(loanDeal.getApprovedAmount())
                .interestRate(loanDeal.getInterestRate())
                .tenureMonths(loanDeal.getTenureMonths())
                .emiAmount(loanDeal.getEmiAmount())
                .sanctionDate(today)
                .status(LoanStatus.GENERATED)
                .remarks("Sanction letter generated successfully")
                .build();

        // 10. Generate and validate PDF
        final Path generatedFile;

        try {
            String pdfPath = pdfGenerator.generate(
                    sanctionLetter,
                    customer
            );

            if (pdfPath == null || pdfPath.isBlank()) {
                throw new IllegalStateException(
                        "PDF generator returned an empty file path"
                );
            }

            generatedFile = Path.of(pdfPath)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.isRegularFile(generatedFile)
                    || !Files.isReadable(generatedFile)) {
                throw new IllegalStateException(
                        "Generated PDF file is missing or unreadable: "
                                + generatedFile
                );
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to generate sanction letter PDF",
                    e
            );
        }

        // 11. Store PDF path
        sanctionLetter.setDocumentPath(generatedFile.toString());

        // 12. Save sanction letter
        SanctionLetter savedLetter =
                sanctionLetterRepository.save(sanctionLetter);

        // 13. Prepare customer name
        String customerName = customer.getFirstName() == null
                ? "Customer"
                : customer.getFirstName().trim();

        // 14. Send PDF attachment through email
        emailService.sendSanctionLetterEmail(
                customer.getEmail(),
                customerName,
                loanDeal.getDealId(),
                generatedFile.toString()
        );

        // 15. Return response
        return sanctionLetterMapper.toResponse(savedLetter);
    }

    // Get existing sanction letter details
    @Override
    @Transactional(readOnly = true)
    public SanctionLetterResponse getSanctionLetter(Long dealId) {

        SanctionLetter sanctionLetter =
                sanctionLetterRepository
                        .findByLoanDeal_DealId(dealId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Sanction letter not found for deal ID: "
                                                + dealId
                                )
                        );

        return sanctionLetterMapper.toResponse(sanctionLetter);
    }

    // Download existing PDF
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadSanctionLetter(Long dealId) {

        SanctionLetter letter = sanctionLetterRepository
                .findByLoanDeal_DealId(dealId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Sanction letter not found for deal ID: "
                                        + dealId
                        )
                );

        String documentPath = letter.getDocumentPath();

        if (documentPath == null || documentPath.isBlank()) {
            throw new IllegalStateException(
                    "PDF path is not available for deal ID: " + dealId
            );
        }

        Path pdfPath = Path.of(documentPath)
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(pdfPath)
                || !Files.isReadable(pdfPath)) {
            throw new IllegalStateException(
                    "PDF file does not exist or is not readable: " + pdfPath
            );
        }

        Resource resource = new FileSystemResource(pdfPath);

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + pdfPath.getFileName() + "\""
                    )
                    .contentLength(Files.size(pdfPath))
                    .body(resource);

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to read PDF file",
                    e
            );
        }
    }
}
