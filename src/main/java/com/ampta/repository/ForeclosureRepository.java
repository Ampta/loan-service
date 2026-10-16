package com.ampta.repository;

import com.ampta.entity.ForeclosureRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ForeclosureRepository
        extends JpaRepository<ForeclosureRequest, Long> {

    List<ForeclosureRequest> findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
            Long loanAccountId
    );

    boolean existsByLoanAccount_LoanAccountIdAndStatusIn(
            Long loanAccountId,
            Collection<String> statuses
    );

    boolean existsByPaymentReference(String reference);
}