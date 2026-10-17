
package com.ampta.repository;

import com.ampta.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanAccountRepository
        extends JpaRepository<LoanAccount, Long> {

    boolean existsByLoanDeal_DealId(Long dealId);

    Optional<LoanAccount> findByLoanDeal_DealId(Long dealId);
}
