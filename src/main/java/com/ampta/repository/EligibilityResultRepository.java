package com.ampta.repository;

import com.ampta.entity.EligibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EligibilityResultRepository
        extends JpaRepository<EligibilityResult, Long> {

    List<EligibilityResult>
    findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<EligibilityResult>
    findFirstByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<EligibilityResult>
    findFirstByCustomer_CustomerIdOrderByEligibilityIdDesc(Long customerId);

    List<EligibilityResult> findAllByOrderByEligibilityIdDesc();
}