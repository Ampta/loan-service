
package com.ampta.repository;

import com.ampta.entity.SanctionLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanctionLetterRepository extends JpaRepository<SanctionLetter, Long> {

    Optional<SanctionLetter> findByLoanDeal_DealId(Long dealId);

    boolean existsByLoanDeal_DealId(Long dealId);
}
