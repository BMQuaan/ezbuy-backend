package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ezbuy.ezbuy.enums.OrderStatus;

@Data
@Builder
public class OrderSummaryResponse {
    private Integer id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
}