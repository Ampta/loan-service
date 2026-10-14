package com.ampta.service.impl;

import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.entity.CibilReport;
import com.ampta.entity.Customer;
import com.ampta.entity.EligibilityResult;
import com.ampta.entity.ScoreCard;
import com.ampta.entity.User;
import com.ampta.entity.enums.RiskCategory;
import com.ampta.entity.enums.Role;
import com.ampta.exception.CibilReportNotFoundException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.EligibilityResultRepository;
import com.ampta.repository.ScoreCardRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.ScoreCardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScoreCardServiceImpl implements ScoreCardService {

    private final ScoreCardRepository scoreCardRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final EligibilityResultRepository eligibilityResultRepository;
    private final UserRepository userRepository;

    // Uses the ModelMapper bean from ModelMapperConfig.
    private final ModelMapper modelMapper;


    @Override
    public ScoreCardResponse generateScoreCard(
            Long customerId,
            Long officerId) {

        validateOfficer(officerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + customerId
                        )
                );

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

        EligibilityResult eligibility = eligibilityResultRepository
                .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Eligibility result not found. "
                                        + "Check eligibility before generating scorecard."
                        )
                );

        Integer score = cibilReport.getCibilScore();

        if (score == null) {
            throw new IllegalArgumentException(
                    "CIBIL score is not available"
            );
        }

        // Update the latest scorecard, or create one if absent.
        ScoreCard scoreCard = scoreCardRepository
                .findFirstByCustomer_CustomerIdOrderByScoreCardIdDesc(
                        customerId
                )
                .orElseGet(ScoreCard::new);

        scoreCard.setCustomer(customer);
        scoreCard.setCibilScore(score);
        scoreCard.setRiskCategory(calculateRiskCategory(score));
        scoreCard.setCurrentStatus(resolveStatus(eligibility));
        scoreCard.setRejectionReason(eligibility.getRejectionReason());
        scoreCard.setEligibleLoanAmount(eligibility.getLoanAmount());

        // The supplied schema has no separate loan application entity/date
        // in this service. Preserve an existing date; otherwise initialise it.
        if (scoreCard.getAppliedDate() == null) {
            scoreCard.setAppliedDate(LocalDateTime.now());
        }

        ScoreCard saved = scoreCardRepository.save(scoreCard);

        log.info(
                "Scorecard generated for customerId={}, scoreCardId={}",
                customerId,
                saved.getScoreCardId()
        );

        return toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public ScoreCardResponse getLatestScoreCard(
            Long customerId,
            Long officerId) {

        validateOfficer(officerId);

        ScoreCard scoreCard = scoreCardRepository
                .findFirstByCustomer_CustomerIdOrderByScoreCardIdDesc(
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Scorecard not found for customer: "
                                        + customerId
                        )
                );

        return toResponse(scoreCard);
    }


    private void validateOfficer(Long officerId) {

        User officer = userRepository.findById(officerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Officer not found: " + officerId
                        )
                );

        if (officer.getRole() != Role.OFFICER) {
            throw new IllegalArgumentException(
                    "Only an officer can access this operation"
            );
        }
    }


    private String resolveStatus(EligibilityResult eligibility) {

        if (Boolean.TRUE.equals(eligibility.getIsEligible())) {
            return "APPROVED";
        }

        if (eligibility.getRejectionReason() != null
                && !eligibility.getRejectionReason().isBlank()) {
            return "REJECTED";
        }

        return "PENDING_REVIEW";
    }


    private RiskCategory calculateRiskCategory(Integer score) {

        // Suggested risk-band policy; adjust independently if required.
        if (score >= 750) {
            return RiskCategory.LOW;
        }

        if (score >= 650) {
            return RiskCategory.MEDIUM;
        }

        return RiskCategory.HIGH;
    }


    private ScoreCardResponse toResponse(ScoreCard scoreCard) {

        // Map the ScoreCard's matching fields.
        ScoreCardResponse response =
                modelMapper.map(
                        scoreCard,
                        ScoreCardResponse.class
                );

        // Map details that come from the associated Customer entity.
        Customer customer = scoreCard.getCustomer();

        response.setCustomerId(customer.getCustomerId());

        String firstName = customer.getFirstName() == null
                ? ""
                : customer.getFirstName().trim();

        String lastName = customer.getLastName() == null
                ? ""
                : customer.getLastName().trim();

        response.setCustomerName(
                (firstName + " " + lastName).trim()
        );

        response.setPanNumber(customer.getPanNumber());
        response.setMonthlyIncome(customer.getMonthlyEarning());
        response.setMonthlySpending(customer.getMonthlySpending());

        return response;
    }
}