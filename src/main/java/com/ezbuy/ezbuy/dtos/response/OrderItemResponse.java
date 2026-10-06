package com.ezbuy.ezbuy.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String productName;
    private int quantity;
    private BigDecimal price; 
    private String productImageUrl;
}