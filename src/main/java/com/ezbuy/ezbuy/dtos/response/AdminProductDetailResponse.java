package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminProductDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private String slug;
    private BigDecimal price;
    private int quantityInStock;
    @Builder.Default
    private boolean isActive = true;
    private String categoryName;
    private String manufacturerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}