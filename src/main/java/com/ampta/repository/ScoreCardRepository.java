package com.ampta.repository;

import com.ampta.entity.ScoreCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreCardRepository
        extends JpaRepository<ScoreCard, Integer> {

    Optional<ScoreCard>
    findFirstByCustomer_CustomerIdOrderByScoreCardIdDesc(
            Long customerId
    );

    List<ScoreCard>
    findByCustomer_CustomerIdOrderByScoreCardIdDesc(
            Long customerId
    );
}