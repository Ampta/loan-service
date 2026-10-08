package com.ampta.config;

import com.ampta.dto.request.LoanApplicationRequest;
import com.ampta.dto.request.LoanApplicationResponse;
import com.ampta.entity.LoanDeal;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    private final ModelMapper modelMapper;

    public LoanMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // Request DTO → Entity
    public LoanDeal toEntity(
            LoanApplicationRequest request) {

        return modelMapper.map(
                request,
                LoanDeal.class
        );
    }

    // Entity → Response DTO
    public LoanApplicationResponse toResponse(
            LoanDeal loanDeal) {

        return modelMapper.map(
                loanDeal,
                LoanApplicationResponse.class
        );
    }
}