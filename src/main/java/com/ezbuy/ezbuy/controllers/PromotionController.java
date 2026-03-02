package com.ezbuy.ezbuy.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ezbuy.ezbuy.dtos.request.PromotionRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionCheckResponse;
import com.ezbuy.ezbuy.dtos.response.PromotionResponse;
import com.ezbuy.ezbuy.services.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "code", "discountValue", "startDate", "endDate", "createdAt", "updatedAt");

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> getAllPromotions(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }
        if (page < 0) page = 0;

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<PromotionResponse> response = promotionService.getAllPromotions(code, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Promotions fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Integer id) {
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success(promotion, "Promotion fetched successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse newPromotion = promotionService.createPromotion(request);
        return new ResponseEntity<>(ApiResponse.success(newPromotion, "Promotion created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Integer id, 
            @Valid @RequestBody PromotionRequest request
    ) {
        PromotionResponse updatedPromotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedPromotion, "Promotion updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Promotion deleted successfully (soft delete)"));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<PromotionCheckResponse>> checkPromotionCode(@RequestParam String code) {
        PromotionCheckResponse checkResponse = promotionService.checkPromotionCode(code);
        return ResponseEntity.ok(ApiResponse.success(checkResponse, "Promotion code is valid."));
    }
}