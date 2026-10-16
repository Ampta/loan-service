package com.ampta.service;


import com.ampta.dto.response.CibilScoreResponse;

import java.util.List;

public interface CibilService {

    // Calculate and save customer's CIBIL score
    CibilScoreResponse calculateCibilScore(Long customerId);

    // Fetch customer's latest CIBIL score and status
//    CibilScoreResponse getLatestCibilScore(Long customerId);
}