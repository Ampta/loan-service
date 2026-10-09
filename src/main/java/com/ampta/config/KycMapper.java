//package com.ampta.config;
//
//import com.ampta.dto.response.KycDocumentResponse;
//import com.ampta.entity.KycDocument;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KycMapper {
//
//    private final ModelMapper modelMapper;
//
//    public KycMapper(ModelMapper modelMapper) {
//        this.modelMapper = modelMapper;
//    }
//
//    public KycDocumentResponse toResponse(KycDocument entity) {
//
//        KycDocumentResponse response =
//                modelMapper.map(entity, KycDocumentResponse.class);
//
//        response.setCustomerId(
//                response.getCustomerId()
//        );
//
//        return response;
//    }
//}