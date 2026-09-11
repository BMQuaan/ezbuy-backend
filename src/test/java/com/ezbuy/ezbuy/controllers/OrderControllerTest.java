package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.request.CreateOrderRequest;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.dtos.response.OrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.exceptions.GlobalExceptionHandler;
import com.ezbuy.ezbuy.services.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/orders: returns 201 Created on valid order creation")
    void createOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentId(1);
        request.setReceiverName("Nguyen Van A");
        request.setShippingAddress("123 Vo Van Ngan, Thu Duc");
        request.setPhone("0987654321");

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(101)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1500))
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class), any(HttpServletRequest.class)))
                .thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderId").value(101))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/orders: returns 400 Bad Request when missing required fields")
    void createOrder_ValidationError() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        // missing receiverName, shippingAddress, phone, paymentId

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/orders/my-orders: returns 200 OK with order summary list")
    void getMyOrders_Success() throws Exception {
        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .id(101)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1500))
                .build();

        PageResponse<OrderSummaryResponse> pageResponse = PageResponse.<OrderSummaryResponse>builder()
                .content(List.of(summary))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(orderService.getMyOrders(eq(null), eq(null), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/orders/my-orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(101));
    }
}
