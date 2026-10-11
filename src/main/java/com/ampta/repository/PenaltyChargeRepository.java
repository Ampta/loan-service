package com.ampta.repository;
import com.ampta.entity.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PenaltyChargeRepository extends JpaRepository<PenaltyCharge, Integer> {
    List<PenaltyCharge> findByEmiSchedule_EmiScheduleId(Integer emiScheduleId);
}
