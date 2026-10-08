package com.ampta.service.impl;

import com.ampta.entity.ScoreCard;
import com.ampta.repository.ScoreCardRepository;
import com.ampta.service.ScoreCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoreCardServiceImpl implements ScoreCardService {

    private final ScoreCardRepository scoreCardRepository;

    @Override
    public ScoreCard createScoreCard(ScoreCard scoreCard) {
        return scoreCardRepository.save(scoreCard);
    }
}
