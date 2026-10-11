package com.ampta.repository;

import com.ampta.entity.LoanDeal;
import com.ampta.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanDealRepository extends JpaRepository<LoanDeal,Long> {

    List<LoanDeal> findByUserId(Long userId);

    List<LoanDeal> findByStatus(LoanStatus status);

    List<LoanDeal> findByUserIdAndStatus(Long userId,LoanStatus status);

}
