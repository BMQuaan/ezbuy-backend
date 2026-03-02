package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PromotionCheckResponse {
    private BigDecimal discountValue; 
}