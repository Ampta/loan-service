package com.ampta.repository;

import com.ampta.entity.DealReview;
import com.ampta.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealReviewRepository extends JpaRepository<DealReview,Long> {

    List<DealReview> findByLoanDealDealId(Long dealId);

    List<DealReview> findByOfficerUserId(Long userId);

    List<DealReview> findByStatus(LoanStatus status);
}
