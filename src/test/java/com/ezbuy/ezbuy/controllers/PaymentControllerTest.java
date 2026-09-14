package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.entities.Order;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentController, "frontendUrl", frontendUrl);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: redirects to frontend purchasehistory with success status")
    void paymentCallback_Success() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);

        mockMvc.perform(get("/api/payments/vnpay-callback")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/profile/purchasehistory/101?paymentStatus=success"));

        verify(orderService).recordVnpayTransaction(101, "14555666");
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: redirects to frontend with failed status on payment error")
    void paymentCallback_Failed() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(0);

        mockMvc.perform(get("/api/payments/vnpay-callback")
                        .param("vnp_TxnRef", "101_1725000000"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/profile/purchasehistory/101?paymentStatus=failed"));

        verify(orderService, never()).recordVnpayTransaction(any(), any());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-callback: returns 400 Bad Request if vnp_TxnRef is missing")
    void paymentCallback_MissingTxnRef() throws Exception {
        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);

        mockMvc.perform(get("/api/payments/vnpay-callback"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/payments/vnpay-ipn: returns 00 Confirm Success on valid IPN")
    void paymentIpn_Success() throws Exception {
        Order order = Order.builder().id(101).vnpTransactionNo(null).build();

        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));

        verify(orderService).recordVnpayTransaction(101, "14555666");
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
    @DisplayName("GET /api/payments/vnpay-ipn: returns 02 Order already confirmed when transaction already recorded")
    void paymentIpn_AlreadyConfirmed() throws Exception {
        Order order = Order.builder().id(101).vnpTransactionNo("EXISTING_TRANSACTION_123").build();

        when(vnpayService.orderReturn(any(HttpServletRequest.class))).thenReturn(1);
        when(orderRepository.findById(101)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "101_1725000000")
                        .param("vnp_TransactionNo", "14555666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("02"))
                .andExpect(jsonPath("$.Message").value("Order already confirmed"));

        verify(orderService, never()).recordVnpayTransaction(any(), any());
    }
}
