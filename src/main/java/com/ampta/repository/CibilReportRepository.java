package com.ampta.repository;
import com.ampta.dto.request.CibilCheckRequest;
import com.ampta.dto.response.CibilScoreResponse;
import com.ampta.entity.CibilReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CibilReportRepository
        extends JpaRepository<CibilReport, Long> {

    Optional<CibilReport>
    findTopByUser_UserIdOrderByCheckDateDesc(Long userId);
}