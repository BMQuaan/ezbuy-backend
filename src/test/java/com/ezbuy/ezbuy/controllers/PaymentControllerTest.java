package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.enums.PaymentStatus;
import com.ezbuy.ezbuy.repositories.OrderRepository;
import com.ezbuy.ezbuy.services.OrderService;
import com.ezbuy.ezbuy.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VNPayService vnpayService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentController paymentController;

    private final String frontendUrl = "http://localhost:3000";
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentController, "frontendUrl", frontendUrl);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();

        sampleOrder = Order.builder()
                .id(101)
                .totalAmount(BigDecimal.valueOf(100)) // 100 USD * 25300 = 2,530,000 VND
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 200 OK with success status on valid payment and amount")
    void paymentCallback_Success() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-callback")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666")
                        .param("vnp_Amount", "253000000")
                        .param("vnp_BankCode", "NCB")
                        .param("vnp_ResponseCode", "00")) // 100 USD * 25300 * 100
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.data.orderId").value(101))
                .andExpect(jsonPath("$.data.transactionNo").value("14555666"));

        verify(orderService).recordVnpayTransaction(eq(101), eq("14555666"), anyMap());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 400 Bad Request when amount does not match")
    void paymentCallback_InvalidAmount() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-callback")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666")
                        .param("vnp_Amount", "100000")) // 1,000 VND instead of 2,530,000 VND
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid payment amount"));

        verify(orderService).recordFailedVnpayTransaction(eq(101), anyMap());
        verify(orderService, never()).recordVnpayTransaction(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 200 OK with failed payment status on user cancellation")
    void paymentCallback_Failed() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(0);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-callback")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_Amount", "253000000")
                        .param("vnp_ResponseCode", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.paymentStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.orderId").value(101));

        verify(orderService).recordFailedVnpayTransaction(eq(101), anyMap());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 400 Bad Request if checksum verification fails")
    void paymentCallback_InvalidChecksum() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(-1);

        mockMvc.perform(get("/api/payments/vnpay-callback"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid Checksum Signature"));
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 400 Bad Request if vnp_TxnRef is missing")
    void paymentCallback_MissingTxnRef() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);

        mockMvc.perform(get("/api/payments/vnpay-callback"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Order reference is invalid or missing in callback"));
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-ipn: returns 00 Confirm Success on valid IPN and matching amount")
    void paymentIpn_Success() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666")
                        .param("vnp_Amount", "253000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));

        verify(orderService).recordVnpayTransaction(eq(101), eq("14555666"), anyMap());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-ipn: returns 04 Invalid Amount on amount mismatch")
    void paymentIpn_InvalidAmount() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_Amount", "100000")) // Mismatched amount
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("04"))
                .andExpect(jsonPath("$.Message").value("Invalid Amount"));

        verify(orderService).recordFailedVnpayTransaction(eq(101), anyMap());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-ipn: returns 97 Invalid Checksum on bad signature")
    void paymentIpn_InvalidChecksum() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(-1);

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97"))
                .andExpect(jsonPath("$.Message").value("Invalid Checksum"));

        verify(orderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-ipn: returns 02 Order already confirmed when paymentStatus is PAID")
    void paymentIpn_AlreadyConfirmed() throws Exception {
        sampleOrder.setPaymentStatus(PaymentStatus.PAID);
        sampleOrder.setVnpTransactionNo("EXISTING_TRANSACTION_123");

        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666")
                        .param("vnp_Amount", "253000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("02"))
                .andExpect(jsonPath("$.Message").value("Order already confirmed"));

        verify(orderService, never()).recordVnpayTransaction(any(), any(), any());
    }
}
