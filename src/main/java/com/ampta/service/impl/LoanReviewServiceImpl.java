package com.ampta.service.impl;

import com.ampta.config.LoanReviewMapper;
import com.ampta.dto.request.LoanReviewRequest;
import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.DealReview;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.User;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.entity.enums.Role;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.DealReviewRepository;
import com.ampta.repository.LoanDealRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.LoanReviewService;
import com.ampta.utils.EmailServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanReviewServiceImpl implements LoanReviewService {

    private final LoanDealRepository loanDealRepository;
    private final DealReviewRepository dealReviewRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final LoanReviewMapper loanReviewMapper;
    private final EmailServiceImpl emailService;

    @Override
    @Transactional
    public LoanReviewResponse reviewLoan(
            Long dealId,
            Long officerId,
            LoanReviewRequest request) {

        // 1. Find loan deal
        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan Deal Not Found with this ID: " + dealId));

        // 2. Find officer
//        Customer officer = customerRepository.findById(officerId)
//                .orElseThrow(() ->
//                        new IllegalArgumentException(
//                                "Officer Not Found with this ID: " + officerId
//                        ));

        User officer=userRepository.findByUserIdAndRole(officerId, Role.OFFICER)
                .orElseThrow(()-> new ResourceNotFoundException("Officer Not Found For This Id :"+ officerId));

        // 3. Validate customer association
        if (loanDeal.getCustomer() == null
                || loanDeal.getCustomer().getCustomerId() == null) {

            throw new IllegalStateException(
                    "Customer is not associated with loan deal ID: " + dealId);
        }

        // 4. Validate current loan status
        if (loanDeal.getStatus() != LoanStatus.SUBMITTED
                && loanDeal.getStatus() != LoanStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Loan cannot be reviewed in current status: "
                            + loanDeal.getStatus());
        }

        // 5. Validate requested review status
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Review status is required");
        }

        if (request.getStatus() != LoanStatus.APPROVED
                && request.getStatus() != LoanStatus.REJECTED) {

            throw new IllegalArgumentException(
                    "Officer can only APPROVE or REJECT a loan");
        }

        // 6. Create review record
        DealReview dealReview = DealReview.builder()
                .loanDeal(loanDeal)
                .officer(officer)
                .status(request.getStatus().name())
                .build();

        // 7. Update loan status
        loanDeal.setStatus(request.getStatus());

        loanDealRepository.save(loanDeal);

        // 8. Save review
        DealReview savedDealReview = dealReviewRepository.save(dealReview);

        // 9. Send approval email
        if (request.getStatus() == LoanStatus.APPROVED) {

            Customer customer = customerRepository
                    .findById(loanDeal.getCustomer().getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException(
                                    "Customer Not Found with ID: " + loanDeal.getCustomer().getCustomerId()));

            emailService.sendLoanApprovedEmail(
                    customer.getEmail(),
                    customer.getFirstName(),
                    loanDeal.getDealId(),
                    loanDeal.getApprovedAmount());
        }

        // 10. Return mapped response
        return loanReviewMapper.toResponse(savedDealReview);
    }
}
