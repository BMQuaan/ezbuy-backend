package com.ezbuy.ezbuy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.ezbuy.ezbuy.dtos.request.PromotionRequest;
import com.ezbuy.ezbuy.dtos.response.PromotionResponse;
import com.ezbuy.ezbuy.entities.Promotion;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    
    Promotion toPromotion(PromotionRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);
    
    void updatePromotionFromRequest(@MappingTarget Promotion promotion, PromotionRequest request);
}