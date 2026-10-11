package com.ampta.service.impl;

import com.ampta.config.LoanReviewMapper;
import com.ampta.dto.request.LoanReviewRequest;
import com.ampta.dto.response.LoanReviewResponse;
import com.ampta.entity.DealReview;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.User;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.exception.LoanNotFoundException;
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
    private final LoanReviewMapper loanReviewMapper;
    private final EmailServiceImpl emailService;

    @Override
    @Transactional
    public LoanReviewResponse reviewLoan(
            Long dealId,
            Long officerId,
            LoanReviewRequest request) {

        LoanDeal loanDeal = loanDealRepository.findById(dealId)
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan Deal Not Found with this id : " + dealId
                        )
                );


        User officer = userRepository.findById(officerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Officer Not Found with this ID : " + officerId
                        )
                );


        if (loanDeal.getStatus() != LoanStatus.SUBMITTED
                && loanDeal.getStatus() != LoanStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Loan cannot be reviewed in current status : "
                            + loanDeal.getStatus()
            );
        }

        if (request.getStatus() != LoanStatus.APPROVED
                && request.getStatus() != LoanStatus.REJECTED) {

            throw new IllegalArgumentException(
                    "Officer can only APPROVE or REJECT a loan"
            );
        }


        DealReview dealReview = DealReview.builder()
                .loanDeal(loanDeal)
                .officer(officer)
                .status(request.getStatus().name())
                .build();


        loanDeal.setStatus(request.getStatus());

        LoanDeal savedLoanDeal = loanDealRepository.save(loanDeal);

        DealReview savedDealReview =
                dealReviewRepository.save(dealReview);

        if (request.getStatus() == LoanStatus.APPROVED) {

            User customer = userRepository
                    .findById(savedLoanDeal.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Customer not found with ID : "
                                            + savedLoanDeal.getUserId()
                            )
                    );

            emailService.sendLoanApprovedEmail(
                    customer.getEmail(),
                    customer.getFirstName(),
                    savedLoanDeal.getDealId(),
                    savedLoanDeal.getApprovedAmount()
            );
        }

        return loanReviewMapper.toResponse(savedDealReview);
    }
}