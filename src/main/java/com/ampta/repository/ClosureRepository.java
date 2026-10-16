package com.ampta.repository;

import com.ampta.entity.ClosureRequest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ClosureRepository
        extends JpaRepository<ClosureRequest, Long> {

    List<ClosureRequest>
    findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
            Long loanAccountId
    );

    boolean existsByLoanAccount_LoanAccountIdAndStatusIn(
            Long loanAccountId,
            Collection<String> statuses
    );

    boolean existsByPaymentReference(String reference);
}