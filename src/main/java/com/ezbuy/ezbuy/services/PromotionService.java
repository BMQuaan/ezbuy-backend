package com.ezbuy.ezbuy.services;

import org.springframework.data.domain.Pageable;

import com.ezbuy.ezbuy.dtos.request.PromotionRequest;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionCheckResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionResponse;

public interface PromotionService {
    PageResponse<PromotionResponse> getAllPromotions(String code, Pageable pageable);
    PromotionResponse getPromotionById(Integer id);
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(Integer id, PromotionRequest request);
    void deletePromotion(Integer id);
    PromotionCheckResponse checkPromotionCode(String code);
}