package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.CreateOrderRequest;
import com.ezbuy.ezbuy.dtos.request.UpdateOrderStatusRequest;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.entities.*;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.mappers.OrderMapper;
import com.ezbuy.ezbuy.repositories.*;
import com.ezbuy.ezbuy.services.impl.OrderServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartCleanupService cartCleanupService;

    @Mock
    private VNPayService vnpayService;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User sampleUser;
    private Product sampleProduct;
    private Payment samplePaymentCOD;
    private Payment samplePaymentVNPay;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1)
                .email("user@example.com")
                .firstName("Van A")
                .lastName("Nguyen")
                .build();

        sampleProduct = Product.builder()
                .id(100)
                .name("iPhone 15")
                .price(BigDecimal.valueOf(1000))
                .quantityInStock(10)
                .isActive(true)
                .build();

        samplePaymentCOD = Payment.builder()
                .id(1)
                .method("COD")
                .build();

        samplePaymentVNPay = Payment.builder()
                .id(2)
                .method("VNPAY")
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(sampleUser);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateOrderRequest buildCreateOrderRequest(Integer paymentId, String promoCode) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentId(paymentId);
        request.setPromoCode(promoCode);
        request.setReceiverName("Van A");
        request.setShippingAddress("123 Street");
        request.setPhone("0123456789");
        return request;
    }

    @Test
    @DisplayName("createOrder: creates COD order, deducts stock and clears cart")
    void createOrder_COD_Success() {
        CreateOrderRequest request = buildCreateOrderRequest(1, null);

        Cart cartItem = Cart.builder()
                .user(sampleUser)
                .product(sampleProduct)
                .quantity(2)
                .build();

        Order savedOrder = Order.builder()
                .id(10)
                .user(sampleUser)
                .payment(samplePaymentCOD)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(2000))
                .orderItems(new ArrayList<>())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(sampleProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(1000))
                .build();
        savedOrder.getOrderItems().add(orderItem);

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(10)
                .totalAmount(BigDecimal.valueOf(2000))
                .build();

        when(cartRepository.countByUser(sampleUser)).thenReturn(1);
        when(cartCleanupService.cleanCartAndGetValidItems(sampleUser)).thenReturn(List.of(cartItem));
        when(paymentRepository.findById(1)).thenReturn(Optional.of(samplePaymentCOD));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request, httpServletRequest);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(10);
        assertThat(sampleProduct.getQuantityInStock()).isEqualTo(8); // 10 - 2

        verify(productRepository).saveAll(anyList());
        verify(cartRepository).deleteByUser(sampleUser);
    }

    @Test
    @DisplayName("createOrder: creates VNPay order and generates payment url")
    void createOrder_VNPay_Success() {
        CreateOrderRequest request = buildCreateOrderRequest(2, null);

        Cart cartItem = Cart.builder()
                .user(sampleUser)
                .product(sampleProduct)
                .quantity(1)
                .build();

        Order savedOrder = Order.builder()
                .id(20)
                .user(sampleUser)
                .payment(samplePaymentVNPay)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1000))
                .orderItems(new ArrayList<>())
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(20)
                .totalAmount(BigDecimal.valueOf(1000))
                .build();

        when(cartRepository.countByUser(sampleUser)).thenReturn(1);
        when(cartCleanupService.cleanCartAndGetValidItems(sampleUser)).thenReturn(List.of(cartItem));
        when(paymentRepository.findById(2)).thenReturn(Optional.of(samplePaymentVNPay));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(vnpayService.createPaymentUrl(anyLong(), anyString(), anyString(), eq(httpServletRequest)))
                .thenReturn("https://sandbox.vnpayment.vn/payment-url");
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request, httpServletRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentUrl()).isEqualTo("https://sandbox.vnpayment.vn/payment-url");
    }

    @Test
    @DisplayName("createOrder: throws exception when cart is empty")
    void createOrder_EmptyCart_ThrowsException() {
        CreateOrderRequest request = buildCreateOrderRequest(1, null);

        when(cartRepository.countByUser(sampleUser)).thenReturn(0);

        assertThatThrownBy(() -> orderService.createOrder(request, httpServletRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cart is empty.");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: applies valid promo code correctly")
    void createOrder_WithPromoCode_Success() {
        CreateOrderRequest request = buildCreateOrderRequest(1, "DISCOUNT10");

        Cart cartItem = Cart.builder()
                .user(sampleUser)
                .product(sampleProduct)
                .quantity(1)
                .build();

        Promotion promotion = Promotion.builder()
                .id(1)
                .code("DISCOUNT10")
                .discountValue(BigDecimal.valueOf(10)) // 10%
                .endDate(LocalDateTime.now().plusDays(5))
                .build();

        Order savedOrder = Order.builder()
                .id(30)
                .user(sampleUser)
                .payment(samplePaymentCOD)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(900)) // 1000 - 100
                .orderItems(new ArrayList<>())
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(30)
                .totalAmount(BigDecimal.valueOf(900))
                .build();

        when(cartRepository.countByUser(sampleUser)).thenReturn(1);
        when(cartCleanupService.cleanCartAndGetValidItems(sampleUser)).thenReturn(List.of(cartItem));
        when(paymentRepository.findById(1)).thenReturn(Optional.of(samplePaymentCOD));
        when(promotionRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(promotion));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request, httpServletRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(900));
    }

    @Test
    @DisplayName("updateOrderStatusByAdmin: confirms PENDING order")
    void updateOrderStatus_Confirm_Success() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        Order order = Order.builder()
                .id(10)
                .status(OrderStatus.PENDING)
                .payment(samplePaymentCOD)
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse.builder().orderId(10).status(OrderStatus.CONFIRMED).build());

        OrderResponse response = orderService.updateOrderStatusByAdmin(10, request);

        assertThat(response).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateOrderStatusByAdmin: throws exception when confirming unpaid VNPay order")
    void updateOrderStatus_UnpaidVNPay_ThrowsException() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        Order order = Order.builder()
                .id(10)
                .status(OrderStatus.PENDING)
                .payment(samplePaymentVNPay)
                .vnpTransactionNo(null) // Unpaid
                .build();

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatusByAdmin(10, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VNPAY order not yet paid");
    }

    @Test
    @DisplayName("cancelMyOrder: cancels order and restores product stock")
    void cancelMyOrder_Success() {
        OrderItem item = OrderItem.builder()
                .product(sampleProduct)
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(10)
                .user(sampleUser)
                .status(OrderStatus.PENDING)
                .orderItems(List.of(item))
                .build();

        sampleProduct.setQuantityInStock(8);

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse.builder().orderId(10).status(OrderStatus.CANCELLED).build());

        orderService.cancelMyOrder(10);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(sampleProduct.getQuantityInStock()).isEqualTo(10); // Restored from 8 -> 10
        verify(productRepository).saveAll(anyList());
    }
}
