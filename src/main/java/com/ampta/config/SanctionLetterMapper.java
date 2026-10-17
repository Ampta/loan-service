package com.ampta.config;

import com.ampta.dto.response.SanctionLetterResponse;
import com.ampta.entity.SanctionLetter;
import org.springframework.stereotype.Component;

@Component
public class SanctionLetterMapper {

    public SanctionLetterResponse toResponse(SanctionLetter letter) {

        SanctionLetterResponse response = new SanctionLetterResponse();

        response.setDealId(letter.getLoanDeal().getDealId());

        response.setSanctionLetterNo(letter.getSanctionLetterNo());
        response.setSanctionedAmount(letter.getSanctionedAmount());
        response.setInterestRate(letter.getInterestRate());
        response.setTenureMonths(letter.getTenureMonths());
        response.setEmiAmount(letter.getEmiAmount());
        response.setSanctionDate(letter.getSanctionDate());
        response.setStatus(letter.getStatus());
        response.setDocumentPath(letter.getDocumentPath());
        response.setRemarks(letter.getRemarks());

        return response;
    }
}
