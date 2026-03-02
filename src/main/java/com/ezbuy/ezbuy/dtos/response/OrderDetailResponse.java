package com.ezbuy.ezbuy.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ezbuy.ezbuy.enums.OrderStatus;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class OrderDetailResponse {
    private Integer id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String receiverName;
    private String shippingAddress;
    private String phone;
    private String note;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
}