package com.ampta.repository;

import com.ampta.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Integer> {
}
