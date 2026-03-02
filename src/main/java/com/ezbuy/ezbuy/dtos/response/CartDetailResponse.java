package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartDetailResponse {
    private List<CartResponse> items;
    private BigDecimal totalCartPrice;
}