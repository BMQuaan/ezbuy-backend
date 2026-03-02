package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private String productName;
    private int quantity;
    private BigDecimal price; 
    private String productImageUrl;
}