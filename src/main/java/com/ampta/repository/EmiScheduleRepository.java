
package com.ampta.repository;

import com.ampta.entity.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Integer> {
    Page<EmiSchedule> findByLoanAccount_LoanAccountId(
            Integer loanAccountId,
            Pageable pageable
    );
    List<EmiSchedule> findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(Integer loanAccountId, String pending);
}
