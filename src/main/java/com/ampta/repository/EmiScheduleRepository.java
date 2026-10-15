
package com.ampta.repository;

import com.ampta.dto.response.EmiScheduleResponse;
import com.ampta.entity.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule,Long> {

    Page<EmiSchedule> findByLoanAccount_LoanAccountId(
            Long loanAccountId,
            Pageable pageable
    );
    boolean existsByLoanAccount_LoanAccountId(Long loanAccountId);

    List<EmiSchedule> findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(Long loanAccountId, String pending);
    @Query("""
    SELECT e
    FROM EmiSchedule e
    WHERE e.loanAccount.loanAccountId = :loanAccountId
    ORDER BY e.installmentNo ASC
    """)
    List<EmiSchedule> getEmiScheduleDetailsByLoanAccountId(
            @Param("loanAccountId") Long loanAccountId
    );


        @Query(value = """
            SELECT *
            FROM emi_schedules
            WHERE due_date < CURRENT_DATE
              AND UPPER(TRIM(payment_status))
                  IN ('PENDING', 'BOUNCED')
            """, nativeQuery = true)
        List<EmiSchedule> findOverdueEmisForTesting();

        List<EmiSchedule> findByPaymentStatusInAndDueDateBefore(
                List<String> statuses,
                LocalDate dueDate
        );

}

