package com.ampta.repository;

import com.ampta.entity.CibilReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CibilReportRepository
        extends JpaRepository<CibilReport, Long> {

    Optional<CibilReport>
    findTopByCustomer_CustomerIdOrderByCheckDateDesc(
            Long customerId
    );

    Optional<CibilReport>
    findFirstByCustomer_CustomerIdOrderByCheckDateDesc(
            Long customerId
    );

    @Query("""
    SELECT report
    FROM CibilReport report
    JOIN FETCH report.customer
    WHERE report.cibilScore >= :minimumScore
      AND report.cibilScore < :maximumScore
      AND report.checkDate = (
          SELECT MAX(latest.checkDate)
          FROM CibilReport latest
          WHERE latest.customer.customerId =
                report.customer.customerId
      )
    ORDER BY report.checkDate DESC
    """)
    List<CibilReport> findLatestReportsBetweenScores(
            @Param("minimumScore") Integer minimumScore,
            @Param("maximumScore") Integer maximumScore
    );
}