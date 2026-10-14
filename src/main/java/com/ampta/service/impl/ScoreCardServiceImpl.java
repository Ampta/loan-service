package com.ampta.service.impl;

import com.ampta.dto.request.ScoreCardRequest;
import com.ampta.dto.response.ScoreCardResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.ScoreCard;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.EligibilityResultRepository;
import com.ampta.repository.ScoreCardRepository;
import com.ampta.service.ScoreCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreCardServiceImpl implements ScoreCardService {

    private final ScoreCardRepository scoreCardRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final EligibilityResultRepository eligibilityResultRepository;

    @Override
    public ScoreCardResponse createOrGenerateScoreCard(ScoreCardRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        ScoreCard scoreCard = new ScoreCard();
        scoreCard.setCustomer(customer);

        Integer score = request.getCibilScore();
        if (score == null) {
            score = cibilReportRepository.findFirstByCustomer_CustomerIdOrderByCheckDateDesc(customer.getCustomerId())
                    .map(c -> c.getCibilScore())
                    .orElse(700);
        }
        scoreCard.setCibilScore(score);

        String risk;
        String status;
        if (score >= 800) {
            risk = "LOW_RISK";
            status = "APPROVED";
        } else if (score >= 650) {
            risk = "MEDIUM_RISK";
            status = "UNDER_REVIEW";
        } else {
            risk = "HIGH_RISK";
            status = "REJECTED";
        }

        scoreCard.setRiskCategory(request.getRiskCategory() != null ? request.getRiskCategory() : risk);
        scoreCard.setCurrentStatus(request.getCurrentStatus() != null ? request.getCurrentStatus() : status);
        scoreCard.setRejectionReason(request.getRejectionReason() != null ? request.getRejectionReason() : (score < 650 ? "Low credit score (<650)" : null));

        Double eligibleAmount = request.getEligibleLoanAmount();
        if (eligibleAmount == null) {
            eligibleAmount = eligibilityResultRepository.findFirstByCustomer_CustomerIdOrderByCreatedAtDesc(customer.getCustomerId())
                    .map(e -> e.getLoanAmount())
                    .orElse(Double.valueOf(500000));
        }
        scoreCard.setEligibleLoanAmount(eligibleAmount);

        ScoreCard saved = scoreCardRepository.save(scoreCard);
        return mapToResponse(saved);
    }

    private ScoreCardResponse mapToResponse(ScoreCard sc) {
        String name = sc.getCustomer() != null ? sc.getCustomer().getFirstName() + (sc.getCustomer().getLastName() != null ? " " + sc.getCustomer().getLastName() : "") : "";
        return ScoreCardResponse.builder()
                .scoreCardId(sc.getScoreCardId())
                .customerId(sc.getCustomer() != null ? sc.getCustomer().getCustomerId() : null)
                .customerName(name)
                .currentStatus(sc.getCurrentStatus())
                .rejectionReason(sc.getRejectionReason())
                .appliedDate(sc.getAppliedDate())
                .cibilScore(sc.getCibilScore())
                .riskCategory(sc.getRiskCategory())
                .eligibleLoanAmount(sc.getEligibleLoanAmount())
                .build();
    }

    @Override
    public ScoreCardResponse getScoreCardById(Integer scoreCardId) {
        return null;
    }

    @Override
    public ScoreCardResponse getCustomerScoreCard(Integer customerId) {
        return null;
    }

    @Override
    public List<ScoreCardResponse> getAllScoreCards() {
        return List.of();
    }

    @Override
    public ScoreCardResponse updateScoreCard(Integer scoreCardId, ScoreCardRequest request) {
        return null;
    }
}
