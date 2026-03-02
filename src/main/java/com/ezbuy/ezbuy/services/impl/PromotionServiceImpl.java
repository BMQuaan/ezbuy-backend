package com.ezbuy.ezbuy.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezbuy.ezbuy.dtos.request.PromotionRequest;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionCheckResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionResponse;
import com.ezbuy.ezbuy.entities.Promotion;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.PromotionMapper;
import com.ezbuy.ezbuy.repositories.PromotionRepository;
import com.ezbuy.ezbuy.repositories.specifications.PromotionSpecification;
import com.ezbuy.ezbuy.services.PromotionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @Override
    public PageResponse<PromotionResponse> getAllPromotions(String code, Pageable pageable) {
        Specification<Promotion> spec = PromotionSpecification.empty();
        
        if (code != null && !code.isEmpty()) {
            spec = spec.and(PromotionSpecification.codeContains(code));
        }

        Page<Promotion> page = promotionRepository.findAll(spec, pageable);
        List<PromotionResponse> content = page.getContent().stream()
                .map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());
        
        return PageResponse.fromPage(page, content);
    }

    @Override
    public PromotionResponse getPromotionById(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        validatePromotionCodeIsUniqueForActive(request.getCode(), null); 
        validatePromotionDates(request); 

        Promotion promotion = promotionMapper.toPromotion(request);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));
        
        validatePromotionCodeIsUniqueForActive(request.getCode(), existingPromotion.getId());
        validatePromotionDates(request);

        promotionMapper.updatePromotionFromRequest(existingPromotion, request);
        Promotion updatedPromotion = promotionRepository.save(existingPromotion);
        return promotionMapper.toPromotionResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found with id: " + id));
        
        promotion.setActive(false);
        promotion.setDeletedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
        
    }

    @Override
    public PromotionCheckResponse checkPromotionCode(String code) {
        LocalDateTime now = LocalDateTime.now(); 
        
        Promotion promotion = promotionRepository.findValidPromotionByCode(code, now)
                .orElseThrow(() -> new NotFoundException("Promotion code is not valid or has expired."));
                
        return PromotionCheckResponse.builder()
                .discountValue(promotion.getDiscountValue())
                .build();
    }

    private void validatePromotionCodeIsUniqueForActive(String code, Integer currentId) {
        if (code == null || code.isEmpty()) {
            return; 
        }

        boolean exists;
        if (currentId == null) { 
            exists = promotionRepository.existsByCode(code);
        } else { 
            exists = promotionRepository.existsByCodeAndIdNot(code, currentId);
        }

        if (exists) {
            throw new DataIntegrityViolationException("Promotion code '" + code + "' already exists for an active promotion.");
        }
    }
    
    private void validatePromotionDates(PromotionRequest request) {
         if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
    }
}