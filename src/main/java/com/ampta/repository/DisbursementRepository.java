package com.ampta.repository;

import com.ampta.entity.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement,Long> {

    boolean existsByLoanDeal_DealId(Long dealId);
}
