package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PromotionResponse {
    private Integer id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Builder.Default
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}