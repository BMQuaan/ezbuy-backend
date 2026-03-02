package com.ezbuy.ezbuy.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ezbuy.ezbuy.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOrderSummaryResponse {
    private Integer id;
    private LocalDateTime orderDate;
    private String receiverName;
    private String userEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
}