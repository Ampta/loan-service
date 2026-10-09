package com.ampta.service;

import com.ampta.dto.request.CibilCheckRequest;
import com.ampta.dto.response.CibilScoreResponse;

import java.util.List;

public interface CibilService {


    CibilScoreResponse calculateCibilScore(Long userId);

    CibilScoreResponse getLatestCibilScore(Long userId);
}