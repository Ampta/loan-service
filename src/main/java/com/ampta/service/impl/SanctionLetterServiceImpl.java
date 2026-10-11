package com.ampta.service.impl;

import com.ampta.config.SanctionLetterMapper;
import com.ampta.dto.response.SanctionLetterResponse;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.SanctionLetter;
import com.ampta.entity.User;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.LoanDealRepository;
import com.ampta.repository.SanctionLetterRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.SanctionLetterService;
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
    private final UserRepository userRepository;

    // Generate sanction letter and PDF
    @Override
    @Transactional
    public SanctionLetterResponse generateSanctionLetter(Long dealId) {

        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan deal not found: " + dealId));

        if (loanDeal.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "Sanction letter can only be generated for approved loans");
        }

        if (sanctionLetterRepository.existsByLoanDeal_DealId(dealId)) {
            throw new IllegalStateException(
                    "Sanction letter already exists for deal ID: " + dealId);
        }

        if (loanDeal.getApprovedAmount() == null
                || loanDeal.getApprovedAmount() <= 0) {
            throw new IllegalStateException(
                    "Approved amount must be set before generating the sanction letter");
        }

        User customer = userRepository.findById(loanDeal.getUserId())
                .orElseThrow(() ->
                        new IllegalStateException("Customer not found"));

        String letterNumber =
                "SL-" + LocalDate.now().getYear() + "-" + dealId;

        SanctionLetter sanctionLetter = SanctionLetter.builder()
                .loanDeal(loanDeal)
                .sanctionLetterNo(letterNumber)
                .sanctionedAmount(loanDeal.getApprovedAmount())
                .interestRate(loanDeal.getInterestRate())
                .tenureMonths(loanDeal.getTenureMonths())
                .emiAmount(loanDeal.getEmiAmount())
                .sanctionDate(LocalDate.now())
                .status(LoanStatus.GENERATED)
                .remarks("Sanction letter generated successfully")
                .build();

        try {
            // Generate HTML-based PDF
            String pdfPath = pdfGenerator.generate(sanctionLetter, customer);

            if (pdfPath == null || pdfPath.isBlank()) {
                throw new IllegalStateException(
                        "PDF generator returned an empty file path");
            }

            Path generatedFile = Path.of(pdfPath)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.isRegularFile(generatedFile)
                    || !Files.isReadable(generatedFile)) {
                throw new IllegalStateException(
                        "Generated PDF file is missing or unreadable");
            }

            sanctionLetter.setDocumentPath(generatedFile.toString());

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to generate sanction letter PDF", e);
        }

        SanctionLetter savedLetter =
                sanctionLetterRepository.save(sanctionLetter);

        return sanctionLetterMapper.toResponse(savedLetter);
    }

    // Get existing sanction letter details
    @Override
    @Transactional(readOnly = true)
    public SanctionLetterResponse getSanctionLetter(Long dealId) {

        SanctionLetter sanctionLetter = sanctionLetterRepository
                .findByLoanDeal_DealId(dealId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Sanction letter not found for deal ID: " + dealId));

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
                                "Sanction letter not found for deal ID: " + dealId));

        String documentPath = letter.getDocumentPath();

        if (documentPath == null || documentPath.isBlank()) {
            throw new IllegalStateException(
                    "PDF path is not available for deal ID: " + dealId);
        }

        Path pdfPath = Path.of(documentPath)
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(pdfPath) || !Files.isReadable(pdfPath)) {
            throw new IllegalStateException(
                    "PDF file does not exist or is not readable: " + pdfPath);
        }

        Resource resource = new FileSystemResource(pdfPath);

        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + pdfPath.getFileName() + "\"")
                    .contentLength(Files.size(pdfPath))
                    .body(resource);

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to read PDF file", e);
        }
    }
}
