package com.ezbuy.ezbuy.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ezbuy.ezbuy.dtos.request.CreateOrderRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateOrderStatusRequest;
import com.ezbuy.ezbuy.dtos.response.AdminOrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.dtos.response.OrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.services.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private static final List<String> ADMIN_ALLOWED_SORT_FIELDS = List.of("id", "orderDate", "totalAmount", "receiverName");

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest 
    ) {
        OrderResponse orderResponse = orderService.createOrder(request, httpRequest);
        
        return new ResponseEntity<>(
            ApiResponse.success(orderResponse, "Order created successfully"), 
            HttpStatus.CREATED
        );
    }

    @GetMapping("/{orderId}/payment-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> getRetryPaymentUrl(
            @PathVariable Integer orderId,
            HttpServletRequest request
    ) {
        String url = orderService.getPaymentUrl(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("paymentUrl", url), 
                "Get the successful payment link."
        ));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatusByAdmin(
            @PathVariable Integer orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse updatedOrder = orderService.updateOrderStatusByAdmin(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order status updated by admin"));
    }

    @PostMapping("/{orderId}/cancel") 
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelMyOrder(@PathVariable Integer orderId) {
        OrderResponse cancelledOrder = orderService.cancelMyOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(cancelledOrder, "Order has been cancelled"));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        PageResponse<OrderSummaryResponse> orders = orderService.getMyOrders(id, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "My orders fetched successfully"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderSummaryResponse>>> getAllOrdersForAdmin(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        if (!ADMIN_ALLOWED_SORT_FIELDS.contains(sortBy)) sortBy = "orderDate";
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<AdminOrderSummaryResponse> orders = orderService.getAllOrdersForAdmin(id, receiverName, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "All orders fetched successfully for admin"));
    }
    
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> getOrderDetail(@PathVariable Integer orderId) {
        Object orderDetail = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(orderDetail, "Order detail fetched successfully"));
    }
}