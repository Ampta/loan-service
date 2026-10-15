package com.ampta.service.impl;

import com.ampta.dto.request.EligibilityCheckRequest;
import com.ampta.dto.request.EligibilityDecisionRequest;
import com.ampta.dto.response.EligibilityResultResponse;
import com.ampta.dto.response.OfficerEligibilityCustomerResponse;
import com.ampta.entity.CibilReport;
import com.ampta.entity.Customer;
import com.ampta.entity.EligibilityResult;
import com.ampta.entity.User;
import com.ampta.entity.enums.NotificationType;
import com.ampta.entity.enums.Role;
import com.ampta.exception.CibilReportNotFoundException;
import com.ampta.exception.RejectionReasonRequiredException;
import com.ampta.exception.ResourceAlreadyExistsException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CibilReportRepository;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.EligibilityResultRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.EligibilityService;
import com.ampta.service.NotificationService;
import com.ampta.utils.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EligibilityServiceImpl implements EligibilityService {

    private static final int AUTO_APPROVAL_SCORE = 800;
    private static final int AUTO_REJECTION_SCORE = 650;

    private static final String AUTO_REJECTION_REASON =
            "CIBIL score is below 650, so the eligibility request "
                    + "has been automatically rejected.";

    private final EligibilityResultRepository eligibilityResultRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // Inject the ModelMapper bean configured in ModelMapperConfig.
    private final ModelMapper modelMapper;


    // =====================================================
    // 1. CHECK ELIGIBILITY
    // =====================================================

    @Override
    public EligibilityResultResponse checkEligibility(
            EligibilityCheckRequest request) {

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: "
                                        + request.getCustomerId()
                        )
                );

        CibilReport report = cibilReportRepository
                .findFirstByCustomer_CustomerIdOrderByCheckDateDesc(
                        customer.getCustomerId()
                )
                .orElseThrow(() ->
                        new CibilReportNotFoundException(
                                "CIBIL report not found for customer: "
                                        + customer.getCustomerId()
                        )
                );

        Integer score = report.getCibilScore();

        if (score == null || score < 300 || score > 900) {
            throw new IllegalArgumentException(
                    "A valid CIBIL score between 300 and 900 is required"
            );
        }

        Optional<EligibilityResult> previousOptional =
                eligibilityResultRepository
                        .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                                customer.getCustomerId()
                        );

        EligibilityResult result;
        boolean newRecord = previousOptional.isEmpty();
        boolean scoreChanged = false;
        boolean previouslyRejected = false;

        if (previousOptional.isPresent()) {

            result = previousOptional.get();

            boolean sameScore =
                    Objects.equals(result.getCibilScore(), score);

            if (sameScore) {

                // Do not repeat emails for an already completed
                // automatic approval.
                if (score >= AUTO_APPROVAL_SCORE
                        && Boolean.TRUE.equals(result.getIsEligible())
                        && !hasText(result.getRejectionReason())) {

                    return toResponse(result);
                }

                // Do not repeat emails for an already completed
                // automatic rejection.
                if (score < AUTO_REJECTION_SCORE
                        && isAutoRejected(result)) {

                    return toResponse(result);
                }

                // For 650–799, preserve an existing officer decision
                // or return the existing pending result.
                if (score >= AUTO_REJECTION_SCORE
                        && score < AUTO_APPROVAL_SCORE) {

                    return toResponse(result);
                }

                previouslyRejected =
                        Boolean.FALSE.equals(result.getIsEligible())
                                && hasText(result.getRejectionReason());

            } else {

                // The latest CIBIL score has changed.
                scoreChanged = true;
            }

        } else {

            result = new EligibilityResult();
            result.setCustomer(customer);
        }

        applyScoreDecision(result, score);

        EligibilityResult saved =
                eligibilityResultRepository.save(result);

        // Automatic decision emails and customer notifications.
        if (score >= AUTO_APPROVAL_SCORE) {

            sendApprovalEmail(saved, true);

        } else if (score < AUTO_REJECTION_SCORE) {

            if (!previouslyRejected) {
                sendRejectionEmail(saved, true);
            }

        } else if (newRecord || scoreChanged) {

            // Only 650–799 goes to officer review.
            notifyOfficerReview(saved);
        }

        return toResponse(saved);
    }


    // =====================================================
    // 2. CHECK THROUGH OFFICER API
    // =====================================================

    @Override
    public EligibilityResultResponse checkEligibilityForOfficer(
            EligibilityCheckRequest request,
            Long officerId) {

//        requireOfficer(officerId);

        return checkEligibility(request);
    }

    // 3. OFFICER'S MANUAL-REVIEW LIST
    @Override
    @Transactional(readOnly = true)
    public List<OfficerEligibilityCustomerResponse>
    getManualReviewCustomers() {

//        requireOfficer(officerId);

        // Only latest CIBIL scores 650–799 are manually reviewed.
        List<CibilReport> reports =
                cibilReportRepository.findLatestReportsBetweenScores(
                        AUTO_REJECTION_SCORE,
                        AUTO_APPROVAL_SCORE
                );

        List<OfficerEligibilityCustomerResponse> response =
                new ArrayList<>();

        for (CibilReport report : reports) {

            Customer customer = report.getCustomer();
            Integer score = report.getCibilScore();

            Optional<EligibilityResult> previousOptional =
                    eligibilityResultRepository
                            .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                                    customer.getCustomerId()
                            );

            EligibilityResult current =
                    previousOptional.orElse(null);

            boolean sameScore =
                    current != null  && Objects.equals(current.getCibilScore(), score);

            // Hide customers whose current score has a final decision.
            if (sameScore && isFinalDecision(current)) {
                continue;
            }

            // Map common fields using ModelMapper.
            OfficerEligibilityCustomerResponse item =
                    modelMapper.map(
                            customer,
                            OfficerEligibilityCustomerResponse.class
                    );

            item.setCustomerName(customerName(customer));
            item.setCibilScore(score);

            item.setEligibilityId(
                    sameScore ? current.getEligibilityId() : null
            );

            item.setDecisionStatus(
                    sameScore ? toResponse(current).getDecisionStatus()
                            : "NOT_CHECKED"
            );

            response.add(item);
        }

        return response;
    }


    // =====================================================
    // 4. OFFICER APPROVES OR REJECTS
    // =====================================================

    @Override
    public EligibilityResultResponse decideEligibility(
            Long eligibilityId,
            Long officerId,
            EligibilityDecisionRequest request) {

//        requireOfficer(officerId);

        EligibilityResult result =
                eligibilityResultRepository.findById(eligibilityId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found: "
                                                + eligibilityId
                                )
                        );

        Integer score = result.getCibilScore();

        if (score == null) {
            throw new IllegalArgumentException(
                    "CIBIL score is not available"
            );
        }

        if (score >= AUTO_APPROVAL_SCORE) {
            throw new ResourceAlreadyExistsException(
                    "CIBIL score is 800 or above. "
                            + "This customer is automatically approved."
            );
        }

        if (score < AUTO_REJECTION_SCORE) {
            throw new ResourceAlreadyExistsException(
                    "CIBIL score below 650 is automatically rejected. "
                            + "Manual decision is not allowed."
            );
        }

        // This endpoint is exclusively for pending 650–799 scores.
        if (isFinalDecision(result)) {
            throw new ResourceAlreadyExistsException(
                    "This eligibility result has already been decided"
            );
        }

        if (request.getApproved() == null) {
            throw new IllegalArgumentException(
                    "Please specify whether to approve or reject"
            );
        }

        if (Boolean.TRUE.equals(request.getApproved())) {

            result.setIsEligible(true);
            result.setRejectionReason(null);
            result.setLoanAmount(calculateLoanAmount(score));

        } else {

            String reason = request.getRejectionReason();

            if (!hasText(reason)) {
                throw new RejectionReasonRequiredException(
                        "Rejection reason is required"
                );
            }

            result.setIsEligible(false);
            result.setRejectionReason(reason.trim());
            result.setLoanAmount(null);
        }

        EligibilityResult saved =
                eligibilityResultRepository.save(result);

        if (Boolean.TRUE.equals(saved.getIsEligible())) {

            sendApprovalEmail(saved, false);

        } else {

            sendRejectionEmail(saved, false);
        }

        return toResponse(saved);
    }


    // =====================================================
    // 5. GET ELIGIBILITY BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public EligibilityResultResponse getEligibilityById(
            Long eligibilityId) {

        EligibilityResult result =
                eligibilityResultRepository.findById(eligibilityId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found: "
                                                + eligibilityId
                                )
                        );

        return toResponse(result);
    }


    // =====================================================
    // 6. GET CUSTOMER'S ELIGIBILITY RESULT
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public EligibilityResultResponse getCustomerEligibility(
            Long customerId) {

        EligibilityResult result =
                eligibilityResultRepository
                        .findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer: "
                                                + customerId
                                )
                        );

        return toResponse(result);
    }


    // =====================================================
    // 7. GET ALL ELIGIBILITY RESULTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityResultResponse> getAllEligibility() {

        return eligibilityResultRepository
                .findAllByOrderByEligibilityIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =====================================================
    // 8. APPLY AUTOMATIC DECISION
    // =====================================================

    private void applyScoreDecision(
            EligibilityResult result,
            Integer score) {

        result.setCibilScore(score);

        if (score >= AUTO_APPROVAL_SCORE) {

            result.setIsEligible(true);
            result.setLoanAmount(calculateLoanAmount(score));
            result.setRejectionReason(null);

        } else if (score < AUTO_REJECTION_SCORE) {

            result.setIsEligible(false);
            result.setLoanAmount(null);
            result.setRejectionReason(AUTO_REJECTION_REASON);

        } else {

            // 650–799: officer review required.
            result.setIsEligible(false);
            result.setLoanAmount(calculateLoanAmount(score));
            result.setRejectionReason(null);
        }
    }

    // =====================================================
    // 9. DECISION HELPERS
    // =====================================================

    private boolean isAutoRejected(EligibilityResult result) {

        return result.getCibilScore() != null
                && result.getCibilScore() < AUTO_REJECTION_SCORE
                && Boolean.FALSE.equals(result.getIsEligible())
                && AUTO_REJECTION_REASON.equals(
                result.getRejectionReason()
        );
    }


    private boolean isFinalDecision(EligibilityResult result) {

        return Boolean.TRUE.equals(result.getIsEligible())
                || hasText(result.getRejectionReason());
    }


    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }


    // =====================================================
    // 10. LOAN AMOUNT
    // =====================================================

    private Double calculateLoanAmount(Integer score) {

        if (score >= 750) {
            return 500000.00;
        } else if (score >= 670) {
            return 300000.00;
        } else if (score >= AUTO_REJECTION_SCORE) {
            return 100000.00;
        }

        return null;
    }


    // =====================================================
    // 11. APPROVAL EMAIL + CUSTOMER NOTIFICATION
    // =====================================================

    private void sendApprovalEmail(
            EligibilityResult result,
            boolean automatic) {

        Customer customer = result.getCustomer();

        emailService.sendEligibilityApprovedEmail(
                customer.getEmail(),
                customerName(customer),
                result.getCibilScore(),
                result.getLoanAmount(),
                automatic
        );

        NotificationType type = automatic
                ? NotificationType.ELIGIBILITY_AUTO_APPROVED
                : NotificationType.ELIGIBILITY_APPROVED;

        String title = automatic
                ? "Loan Eligibility Automatically Approved"
                : "Loan Eligibility Approved";

        String message = automatic
                ? "Your CIBIL score qualifies you for automatic eligibility approval."
                : "Your loan eligibility has been approved by the loan officer.";

        notifyCustomer(customer, type, title, message, result.getEligibilityId());
    }


    // =====================================================
    // 12. REJECTION EMAIL + CUSTOMER NOTIFICATION
    // =====================================================

    private void sendRejectionEmail(
            EligibilityResult result,
            boolean automatic) {

        Customer customer = result.getCustomer();

        emailService.sendEligibilityRejectedEmail(
                customer.getEmail(),
                customerName(customer),
                result.getCibilScore(),
                result.getRejectionReason()
        );

        NotificationType type = automatic
                ? NotificationType.ELIGIBILITY_AUTO_REJECTED
                : NotificationType.ELIGIBILITY_REJECTED;

        String title = automatic
                ? "Loan Eligibility Automatically Rejected"
                : "Loan Eligibility Rejected";

        String message = automatic
                ? AUTO_REJECTION_REASON
                : "Your loan eligibility was rejected. Reason: "
                + result.getRejectionReason();

        notifyCustomer(customer, type, title, message, result.getEligibilityId());
    }


    // =====================================================
    // 13. NOTIFY CUSTOMER'S LINKED USER ACCOUNT
    // =====================================================

    private void notifyCustomer(
            Customer customer,
            NotificationType type,
            String title,
            String message,
            Long eligibilityId) {

        userRepository.findByCustomer_CustomerId(customer.getCustomerId())
                .ifPresentOrElse(
                        user -> notificationService.sendNotification(
                                user.getUserId(),
                                type,
                                title,
                                message,
                                eligibilityId
                        ),
                        () -> log.warn(
                                "No linked user account for customer {}. "
                                        + "Eligibility notification not saved.",
                                customer.getCustomerId()
                        )
                );
    }


    // =====================================================
    // 14. NOTIFY OFFICER OF PENDING REVIEW
    // =====================================================

    private void notifyOfficerReview(EligibilityResult result) {

        userRepository.findByRole(Role.OFFICER)
                .ifPresentOrElse(
                        officer -> notificationService.sendNotification(
                                officer.getUserId(),
                                NotificationType.ELIGIBILITY_REVIEW_REQUIRED,
                                "Eligibility Review Required",
                                "Customer " + customerName(result.getCustomer())
                                        + " has CIBIL score "
                                        + result.getCibilScore()
                                        + " and requires a manual eligibility decision.",
                                result.getEligibilityId()
                        ),
                        () -> log.warn(
                                "No officer account found for eligibility {}",
                                result.getEligibilityId()
                        )
                );
    }


    // =====================================================
    // 15. CUSTOMER NAME
    // =====================================================

    private String customerName(Customer customer) {

        String first = customer.getFirstName() == null
                ? ""
                : customer.getFirstName().trim();

        String last = customer.getLastName() == null
                ? ""
                : customer.getLastName().trim();

        return (first + " " + last).trim();
    }


    // =====================================================
    // 16. ENTITY TO RESPONSE USING MODELMAPPER
    // =====================================================

    private EligibilityResultResponse toResponse(
            EligibilityResult result) {

        EligibilityResultResponse response =
                modelMapper.map(
                        result,
                        EligibilityResultResponse.class
                );

        // ModelMapper maps direct fields. Nested Customer fields
        // are explicitly populated here.
        Customer customer = result.getCustomer();

        response.setCustomerId(customer.getCustomerId());
        response.setCustomerName(customerName(customer));
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setMonthlyEarning(customer.getMonthlyEarning());
        response.setMonthlySpending(customer.getMonthlySpending());

        Integer score = result.getCibilScore();

        if (Boolean.TRUE.equals(result.getIsEligible())) {

            response.setDecisionStatus(
                    score != null && score >= AUTO_APPROVAL_SCORE
                            ? "AUTO_APPROVED"
                            : "APPROVED"
            );

        } else if (isAutoRejected(result)) {

            response.setDecisionStatus("AUTO_REJECTED");

        } else if (hasText(result.getRejectionReason())) {

            response.setDecisionStatus("REJECTED");

        } else {

            response.setDecisionStatus("PENDING_REVIEW");
        }

        return response;
    }
}