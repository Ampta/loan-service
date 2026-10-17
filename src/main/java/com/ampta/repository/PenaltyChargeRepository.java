package com.ampta.repository;
import com.ampta.entity.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyChargeRepository extends JpaRepository<PenaltyCharge, Long> {
 //   List<PenaltyCharge> findByEmiSchedule_EmiScheduleId(Long emiScheduleId);
    Optional<PenaltyCharge> findByEmiSchedule_EmiScheduleId(Long emiScheduleId);


}
