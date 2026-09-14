package com.ezbuy.ezbuy.services;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @InjectMocks
    private VNPayService vnpayService;

    private final String secretKey = "VNPAY_SECRET_KEY_TEST_123456";
    private final String tmnCode = "EZBUYTMN";
    private final String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private final String returnUrl = "http://localhost:3000/payment/vnpay-return";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vnpayService, "vnp_HashSecret", secretKey);
        ReflectionTestUtils.setField(vnpayService, "vnp_TmnCode", tmnCode);
        ReflectionTestUtils.setField(vnpayService, "vnp_PayUrl", payUrl);
        ReflectionTestUtils.setField(vnpayService, "vnp_ReturnUrl", returnUrl);
    }

    @Test
    @DisplayName("createPaymentUrl: generates valid payment URL with signature")
    void createPaymentUrl_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String url = vnpayService.createPaymentUrl(100000L, "Pay for order 10", "10_1725000000", request);

        assertThat(url).isNotNull();
        assertThat(url).startsWith(payUrl);
        assertThat(url).contains("vnp_TmnCode=" + tmnCode);
        assertThat(url).contains("vnp_Amount=10000000"); // 100000 * 100
        assertThat(url).contains("vnp_TxnRef=10_1725000000");
        assertThat(url).contains("vnp_CurrCode=USD");
        assertThat(url).contains("vnp_SecureHash=");
    }

    @Test
    @DisplayName("orderReturn: returns 1 on valid checksum and responseCode 00")
    void orderReturn_Success() {
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "10000000");
        fields.put("vnp_BankCode", "NCB");
        fields.put("vnp_OrderInfo", "Pay for order 10");
        fields.put("vnp_ResponseCode", "00");
        fields.put("vnp_TransactionNo", "14000000");
        fields.put("vnp_TransactionStatus", "00");
        fields.put("vnp_TxnRef", "10_1725000000");

        String calculatedHash = vnpayService.hashAllFields(fields);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(new Vector<>(fields.keySet()).elements());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        when(request.getParameter("vnp_SecureHash")).thenReturn(calculatedHash);
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionStatus")).thenReturn("00");

        int result = vnpayService.orderReturn(request);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("orderReturn: returns -1 on invalid checksum signature")
    void orderReturn_InvalidChecksum_ReturnsMinusOne() {
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "10000000");
        fields.put("vnp_ResponseCode", "00");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(new Vector<>(fields.keySet()).elements());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        when(request.getParameter("vnp_SecureHash")).thenReturn("INVALID_TAMPERED_HASH");

        int result = vnpayService.orderReturn(request);

        assertThat(result).isEqualTo(-1);
    }

    @Test
    @DisplayName("orderReturn: returns 0 on valid checksum but failed responseCode")
    void orderReturn_PaymentFailed_ReturnsZero() {
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "10000000");
        fields.put("vnp_ResponseCode", "24"); // Customer cancelled
        fields.put("vnp_TransactionStatus", "02");

        String calculatedHash = vnpayService.hashAllFields(fields);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterNames()).thenReturn(new Vector<>(fields.keySet()).elements());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        when(request.getParameter("vnp_SecureHash")).thenReturn(calculatedHash);
        when(request.getParameter("vnp_ResponseCode")).thenReturn("24");
        when(request.getParameter("vnp_TransactionStatus")).thenReturn("02");

        int result = vnpayService.orderReturn(request);

        assertThat(result).isEqualTo(0);
    }
}
