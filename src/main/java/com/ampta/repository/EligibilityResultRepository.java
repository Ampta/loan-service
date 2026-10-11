package com.ampta.repository;

import com.ampta.entity.EligibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EligibilityResultRepository extends JpaRepository<EligibilityResult, Long> {
    List<EligibilityResult> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<EligibilityResult> findFirstByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
}
