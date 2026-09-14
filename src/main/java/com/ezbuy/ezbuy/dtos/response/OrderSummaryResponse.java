package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.enums.PaymentStatus;

@Data
@Builder
public class OrderSummaryResponse {
    private Integer id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
}