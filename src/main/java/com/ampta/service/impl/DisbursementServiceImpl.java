 package com.ampta.service.impl;

import com.ampta.config.DisbursementMapper;
import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.DisbursementResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.Disbursement;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.DisbursementRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.LoanDealRepository;
import com.ampta.service.DisbursementService;
import com.ampta.utils.EmailServiceImpl;
import com.ampta.utils.LoanAccountNumberGenerator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final LoanDealRepository loanDealRepository;
    private final DisbursementRepository disbursementRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanAccountNumberGenerator loanAccountNumberGenerator;
    private final DisbursementMapper disbursementMapper;
    private final CustomerRepository customerRepository;
    private final EmailServiceImpl emailService;

    @Override
    @Transactional
    public DisbursementResponse disburseLoan(
            DisbursementRequest request) {

        // 1. Find loan deal
        LoanDeal loanDeal = loanDealRepository
                .findById(request.getDealId())
                .orElseThrow(() ->
                        new LoanNotFoundException(
                                "Loan Deal not found: "
                                        + request.getDealId()
                        )
                );

        // 2. Validate loan status
        if (loanDeal.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only approved loans can be disbursed. Current status: "
                            + loanDeal.getStatus()
            );
        }

        // 3. Validate approved amount
//        Double approvedAmount = loanDeal.getApprovedAmount();
        Double approvedAmount = loanDeal.getLoanAmount();

        if (approvedAmount == null || approvedAmount <= 0) {
            throw new IllegalStateException(
                    "Approved loan amount must be greater than zero"
            );
        }

        // 4. Check existing disbursement
        if (disbursementRepository
                .existsByLoanDeal_DealId(request.getDealId())) {

            throw new IllegalStateException(
                    "Disbursement already exists for deal ID: "
                            + request.getDealId()
            );
        }

        // 5. Check existing loan account
        if (loanAccountRepository
                .existsByLoanDeal_DealId(request.getDealId())) {

            throw new IllegalStateException(
                    "Loan account already exists for deal ID: "
                            + request.getDealId()
            );
        }

        // 6. Validate customer association
        if (loanDeal.getCustomer() == null
                || loanDeal.getCustomer().getCustomerId() == null) {

            throw new IllegalStateException(
                    "Customer is not associated with loan deal ID: "
                            + request.getDealId()
            );
        }

        Long customerId = loanDeal.getCustomer().getCustomerId();

        // 7. Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Customer not found with ID: " + customerId
                        )
                );

        // 8. Validate disbursement date
        if (request.getDisbursementDate() == null) {
            throw new IllegalArgumentException(
                    "Disbursement date is required"
            );
        }

        LocalDateTime disbursementDate =
                request.getDisbursementDate().atStartOfDay();

        // 9. Create and save disbursement
        Disbursement disbursement = Disbursement.builder()
                .loanDeal(loanDeal)
                .disburseAmount(approvedAmount)
                .bankPartner(
                        request.getBankPartner() == null
                                ? null
                                : request.getBankPartner().trim()
                )
                .disbursementDate(disbursementDate)
                .status(LoanStatus.DISBURSED)
                .build();

        Disbursement savedDisbursement =
                disbursementRepository.saveAndFlush(disbursement);

        // 10. Create and save loan account
        LoanAccount loanAccount = LoanAccount.builder()
                .customer(customer)
                .loanDeal(loanDeal)
                .loanAccountNo("TEMP-" + UUID.randomUUID())
                .loanAmount(approvedAmount)
                .outstandingPrincipal(approvedAmount)
                .loanStatus("ACTIVE")
                .interestRate(loanDeal.getInterestRate())
                .emiAmount(loanDeal.getEmiAmount())
                .tenureMonths(loanDeal.getTenureMonths())
                .disbursementDate(disbursementDate)
                .totalPaidAmount(0.0)
                .missedEmiCount(0)
                .build();

        LoanAccount savedLoanAccount =
                loanAccountRepository.saveAndFlush(loanAccount);

        // 11. Generate final loan account number
        String accountNumber = loanAccountNumberGenerator.generate(
                savedLoanAccount.getLoanAccountId()
        );

        savedLoanAccount.setLoanAccountNo(accountNumber);

        savedLoanAccount =
                loanAccountRepository.saveAndFlush(savedLoanAccount);

        // 12. Update loan deal status
        loanDeal.setStatus(LoanStatus.DISBURSED);
        loanDealRepository.save(loanDeal);

        // 13. Build response using the mapper
        DisbursementResponse response =
                disbursementMapper.toResponse(
                        savedDisbursement,
                        savedLoanAccount
                );

        response.setMessage(
                "Loan disbursed and loan account created successfully"
        );

        // 14. Send email
        emailService.sendLoanDisbursementEmail(
                customer.getEmail(),
                customer.getFirstName(),
                loanDeal.getDealId(),
                savedDisbursement.getDisburseAmount(),
                savedLoanAccount.getLoanAccountNo(),
                savedDisbursement.getBankPartner(),
                savedDisbursement.getDisbursementDate()
                        .toLocalDate()
                        .toString()
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisbursementResponse> getAllDisbursement() {

        return disbursementRepository.findAll()
                .stream()
                .map(disbursement -> {

                    LoanAccount loanAccount = null;

                    if (disbursement.getLoanDeal() != null) {
                        loanAccount =
                                loanAccountRepository
                                        .findByLoanDeal_DealId(
                                                disbursement.getLoanDeal()
                                                        .getDealId()
                                        )
                                        .orElse(null);
                    }

                    return disbursementMapper.toResponse(
                            disbursement,
                            loanAccount
                    );
                })
                .toList();
    }
}
