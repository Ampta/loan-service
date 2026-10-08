package com.ampta.repository;

import com.ampta.entity.ScoreCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreCardRepository extends JpaRepository<ScoreCard, Long> {
}
