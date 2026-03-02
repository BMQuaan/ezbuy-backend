package com.ezbuy.ezbuy.services;

import org.springframework.data.domain.Pageable;

import com.ezbuy.ezbuy.dtos.request.CreateOrderRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateOrderStatusRequest;
import com.ezbuy.ezbuy.dtos.response.AdminOrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.dtos.response.OrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.enums.OrderStatus;

import jakarta.servlet.http.HttpServletRequest;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, HttpServletRequest httpRequest);
    String getPaymentUrl(Integer orderId, HttpServletRequest request);
    void recordVnpayTransaction(Integer orderId, String transactionNo);
    OrderResponse updateOrderStatusByAdmin(Integer orderId, UpdateOrderStatusRequest request);
    OrderResponse cancelMyOrder(Integer orderId);

    PageResponse<OrderSummaryResponse> getMyOrders(Integer id, OrderStatus status, Pageable pageable);
    PageResponse<AdminOrderSummaryResponse> getAllOrdersForAdmin(Integer id, String receiverName, OrderStatus status, Pageable pageable);
    Object getOrderDetail(Integer orderId);
}