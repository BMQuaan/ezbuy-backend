package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.enums.PaymentStatus;

@Data
@Builder
public class OrderResponse {
    private Integer orderId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
    private String paymentUrl;
}