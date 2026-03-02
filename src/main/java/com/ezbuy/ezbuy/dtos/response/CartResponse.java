package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartResponse {
    private Integer cartId;
    private Integer productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private int quantity;
    private BigDecimal totalPrice; 
}