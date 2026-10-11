package com.ampta.service.impl;

import com.ampta.config.DisbursementMapper;
import com.ampta.dto.request.DisbursementRequest;
import com.ampta.dto.response.DisbursementResponse;
import com.ampta.entity.Disbursement;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.LoanDeal;
import com.ampta.entity.User;
import com.ampta.entity.enums.LoanStatus;
import com.ampta.exception.LoanNotFoundException;
import com.ampta.repository.DisbursementRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.LoanDealRepository;
import com.ampta.repository.UserRepository;
import com.ampta.service.DisbursementService;
import com.ampta.utils.EmailServiceImpl;
import com.ampta.utils.LoanAccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisbursementServiceImpl implements DisbursementService {

    private final LoanDealRepository loanDealRepository;

    private  final DisbursementRepository disbursementRepository;

    private final LoanAccountRepository loanAccountRepository;

    private final LoanAccountNumberGenerator loanAccountNumberGenerator;

    private final DisbursementMapper disbursementMapper;

    private final UserRepository userRepository;

    private final EmailServiceImpl emailService;
    @Override
    public DisbursementResponse disburseLoan(DisbursementRequest request) {

        LoanDeal loanDeal=loanDealRepository.findById(request.getDealId())
                .orElseThrow(()-> new LoanNotFoundException("Loan Deal not found :"+request.getDealId()));

        if(loanDeal.getStatus() != LoanStatus.APPROVED)
        {
            throw new IllegalStateException(
                    "Only approved loans can be disbursed. Current status: "
                            + loanDeal.getStatus());
        }

        Double approvedAmount=loanDeal.getApprovedAmount();

        if (disbursementRepository.existsByLoanDeal_DealId(request.getDealId()))
        {
            throw new IllegalStateException(
                    "Disbursement already exists for deal ID: "
                            + request.getDealId());
        }

        if(loanAccountRepository.existsByLoanDeal_DealId(request.getDealId()))
        {
            throw new IllegalStateException(
                    "Loan account already exists for deal ID: "
                            + request.getDealId());
        }

        User customer=userRepository.findById(loanDeal.getUserId())

             .orElseThrow(() -> new IllegalStateException(
                "Customer not found for loan deal"));

        LocalDateTime disbursementDate =
                request.getDisbursementDate().atStartOfDay();

        Disbursement disbursement=Disbursement.builder()
                .loanDeal(loanDeal)
                .disburseAmount(approvedAmount)
                .bankPartner(request.getBankPartner().trim())
                .disbursementDate(disbursementDate)
                .status(LoanStatus.DISBURSED)
                .build();

        Disbursement savedDisbursement = disbursementRepository.saveAndFlush(disbursement);


        LoanAccount loanAccount=LoanAccount.builder()
                .customer(customer)
                .loanDeal(loanDeal)
                .userId(loanDeal.getUserId())
                .loanAccountNo("TEMP-" + UUID.randomUUID())
                .loanAmount(approvedAmount)
                .outstandingPrincipal(approvedAmount)
                .loanStatus("ACTIVE")
                .interestRate(loanDeal.getInterestRate())
                .emiAmount(loanDeal.getEmiAmount())
                .tenureMonths(loanDeal.getTenureMonths())
                .disbursementDate(disbursementDate)
                .totalPaidAmount(approvedAmount)
                .build();

        LoanAccount saveloanAccount=loanAccountRepository.save(loanAccount);


        saveloanAccount.setLoanAccountNo(
                loanAccountNumberGenerator.generate(
                        Long.valueOf(saveloanAccount.getLoanAccountId())));

        loanAccountRepository.save(saveloanAccount);

        loanDeal.setStatus(LoanStatus.DISBURSED);
        loanDealRepository.save(loanDeal);

        emailService.sendLoanDisbursementEmail(
                customer.getEmail(),
                customer.getFirstName(),
                loanDeal.getDealId(),
                savedDisbursement.getDisburseAmount(),
                saveloanAccount.getLoanAccountNo(),
                savedDisbursement.getBankPartner(),
                savedDisbursement.getDisbursementDate().toLocalDate().toString()
        );

        DisbursementResponse response =
                disbursementMapper.toResponse(savedDisbursement);

        response.setLoanAccountNo(saveloanAccount.getLoanAccountNo());
        response.setMessage(
                "Loan disbursed and loan account created successfully");

        return response;

    }
}
