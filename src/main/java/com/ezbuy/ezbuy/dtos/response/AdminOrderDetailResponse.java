package com.ezbuy.ezbuy.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ezbuy.ezbuy.enums.OrderStatus;

@Data
@Builder
public class AdminOrderDetailResponse {
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

    private String userFullName;
    private String userEmail;

    private String confirmedByName;
    private LocalDateTime confirmedAt;
}