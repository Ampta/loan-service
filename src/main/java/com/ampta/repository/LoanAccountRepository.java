package com.ampta.repository;

import com.ampta.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from LoanAccount a where a.loanAccountId = :id")
    Optional<LoanAccount> findByIdForUpdate(@Param("id") Long id);
}
