package com.ampta.service.impl;

import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.dto.response.ScoreCardResponse;

import com.ampta.entity.CibilReport;
import com.ampta.entity.Customer;
import com.ampta.entity.EligibilityResult;
import com.ampta.entity.ScoreCard;

import com.ampta.entity.enums.RiskCategory;

import com.ampta.exception.CibilReportNotFoundException;
import com.ampta.exception.ResourceNotFoundException;

import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.EligibilityResultRepository;
import com.ampta.repository.ScoreCardRepository;

import com.ampta.service.ScoreCardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScoreCardServiceImpl implements ScoreCardService {

    private static final int AUTO_APPROVAL_SCORE = 800;
    private static final int AUTO_REJECTION_SCORE = 650;

    private final ScoreCardRepository scoreCardRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final EligibilityResultRepository eligibilityResultRepository;
    private final ModelMapper modelMapper;


    // =====================================================
    // 1. GET ALL CUSTOMERS FOR OFFICER'S SCORECARD LIST
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<OfficerEligibilityCustomerResponse>
    getCustomersForScoreCards() {

        List<Customer> customers = customerRepository.findAll();

        List<OfficerEligibilityCustomerResponse> response =
                new ArrayList<>();

        for (Customer customer : customers) {

            OfficerEligibilityCustomerResponse item =
                    modelMapper.map(
                            customer,
                            OfficerEligibilityCustomerResponse.class
                    );

            item.setCustomerName(customerName(customer));

            // Fetch the latest CIBIL report, if available.
            Optional<CibilReport> reportOptional =
                    cibilReportRepository
                            .findFirstByCustomer_CustomerIdOrderByCheckDateDesc(
                                    customer.getCustomerId()
                            );

            Integer score = reportOptional
                    .map(CibilReport::getCibilScore)
                    .orElse(null);

            item.setCibilScore(score);

            // Fetch the latest eligibility result, if available.
            Optional<EligibilityResult> eligibilityOptional =
                    eligibilityResultRepository
                            .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                                    customer.getCustomerId()
                            );

            EligibilityResult eligibility =
                    eligibilityOptional.orElse(null);

            boolean sameScore =
                    eligibility != null
                            && Objects.equals(
                            eligibility.getCibilScore(),
                            score
                    );

            item.setEligibilityId(
                    sameScore
                            ? eligibility.getEligibilityId()
                            : null
            );

            if (score == null) {

                item.setDecisionStatus("CIBIL_NOT_AVAILABLE");

            } else if (!sameScore) {

                item.setDecisionStatus("NOT_CHECKED");

            } else {

                item.setDecisionStatus(
                        resolveEligibilityStatus(eligibility)
                );
            }

            response.add(item);
        }

        return response;
    }


    // =====================================================
    // 2. VIEW SCORECARD
    // Automatically create or refresh it when required.
    // =====================================================

    @Override
    public ScoreCardResponse getOrGenerateScoreCard(
            Long customerId) {

        // Step 1: Find the selected customer.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + customerId
                        )
                );

        // Step 2: Find the customer's latest CIBIL report.
        CibilReport cibilReport = cibilReportRepository
                .findFirstByCustomer_CustomerIdOrderByCheckDateDesc(
                        customerId
                )
                .orElseThrow(() ->
                        new CibilReportNotFoundException(
                                "CIBIL report not found for customer: "
                                        + customerId
                        )
                );

        Integer score = cibilReport.getCibilScore();

        if (score == null || score < 300 || score > 900) {
            throw new IllegalArgumentException(
                    "A valid CIBIL score between 300 and 900 is required"
            );
        }

        // Step 3: Find the latest eligibility result.
        EligibilityResult eligibility = eligibilityResultRepository
                .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Eligibility result not found for customer: "
                                        + customerId
                                        + ". Check eligibility first."
                        )
                );

        // Do not combine a new CIBIL score with an older eligibility result.
        if (!Objects.equals(eligibility.getCibilScore(), score)) {
            throw new IllegalStateException(
                    "Eligibility has not been checked for the latest "
                            + "CIBIL score. Check eligibility first."
            );
        }

        // Step 4: Update the latest scorecard or create one if absent.
        ScoreCard scoreCard = scoreCardRepository
                .findFirstByCustomer_CustomerIdOrderByScoreCardIdDesc(
                        customerId
                )
                .orElseGet(ScoreCard::new);

        scoreCard.setCustomer(customer);
        scoreCard.setCibilScore(score);
        scoreCard.setRiskCategory(calculateRiskCategory(score));
        scoreCard.setCurrentStatus(resolveStatus(eligibility));
        scoreCard.setRejectionReason(
                eligibility.getRejectionReason()
        );
        scoreCard.setEligibleLoanAmount(
                eligibility.getLoanAmount()
        );

        // Preserve the existing date when refreshing the scorecard.
        if (scoreCard.getAppliedDate() == null) {
            scoreCard.setAppliedDate(LocalDateTime.now());
        }

        // Step 5: Save the scorecard.
        ScoreCard saved = scoreCardRepository.save(scoreCard);

        log.info(
                "Scorecard created/refreshed for customerId={}, scoreCardId={}",
                customerId,
                saved.getScoreCardId()
        );

        // Step 6: Return the scorecard response.
        return toResponse(saved);
    }


    // =====================================================
    // 3. RESOLVE ELIGIBILITY STATUS
    // Used when building the customer list.
    // =====================================================

    private String resolveEligibilityStatus(
            EligibilityResult eligibility) {

        Integer score = eligibility.getCibilScore();

        if (Boolean.TRUE.equals(eligibility.getIsEligible())) {

            return score != null && score >= AUTO_APPROVAL_SCORE
                    ? "AUTO_APPROVED"
                    : "APPROVED";
        }

        if (score != null
                && score < AUTO_REJECTION_SCORE
                && hasText(eligibility.getRejectionReason())) {

            return "AUTO_REJECTED";
        }

        if (hasText(eligibility.getRejectionReason())) {
            return "REJECTED";
        }

        return "PENDING_REVIEW";
    }


    // =====================================================
    // 4. RESOLVE SCORECARD STATUS
    // =====================================================

    private String resolveStatus(EligibilityResult eligibility) {

        Integer score = eligibility.getCibilScore();

        if (Boolean.TRUE.equals(eligibility.getIsEligible())) {

            return score != null && score >= AUTO_APPROVAL_SCORE
                    ? "AUTO_APPROVED"
                    : "APPROVED";
        }

        if (score != null
                && score < AUTO_REJECTION_SCORE
                && hasText(eligibility.getRejectionReason())) {

            return "AUTO_REJECTED";
        }

        if (hasText(eligibility.getRejectionReason())) {
            return "REJECTED";
        }

        return "PENDING_REVIEW";
    }


    // =====================================================
    // 5. CALCULATE RISK CATEGORY
    // =====================================================

    private RiskCategory calculateRiskCategory(Integer score) {

        if (score >= 750) {
            return RiskCategory.LOW;
        }

        if (score >= AUTO_REJECTION_SCORE) {
            return RiskCategory.MEDIUM;
        }

        return RiskCategory.HIGH;
    }


    // =====================================================
    // 6. CUSTOMER NAME
    // =====================================================

    private String customerName(Customer customer) {

        String firstName = customer.getFirstName() == null
                ? ""
                : customer.getFirstName().trim();

        String lastName = customer.getLastName() == null
                ? ""
                : customer.getLastName().trim();

        return (firstName + " " + lastName).trim();
    }


    // =====================================================
    // 7. CHECK NON-EMPTY TEXT
    // =====================================================

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }


    // =====================================================
    // 8. ENTITY TO RESPONSE USING MODELMAPPER
    // =====================================================

    private ScoreCardResponse toResponse(ScoreCard scoreCard) {

        ScoreCardResponse response =
                modelMapper.map(
                        scoreCard,
                        ScoreCardResponse.class
                );

        Customer customer = scoreCard.getCustomer();

        response.setCustomerId(customer.getCustomerId());
        response.setCustomerName(customerName(customer));
        response.setPanNumber(customer.getPanNumber());
        response.setMonthlyIncome(customer.getMonthlyEarning());
        response.setMonthlySpending(customer.getMonthlySpending());

        return response;
    }
}