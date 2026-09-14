package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.PaymentResultResponse;
import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.enums.PaymentStatus;
import com.ezbuy.ezbuy.repositories.OrderRepository;
import com.ezbuy.ezbuy.services.OrderService;
import com.ezbuy.ezbuy.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnpayService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("25300");

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${vnpay.currency:VND}")
    private String vnpayCurrency = "VND";

    /**
     * Client-side return URL: VNPay redirects user's browser back to this endpoint
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<?>> paymentCallback(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);
        Map<String, String> vnpParams = extractParams(request);

        if (paymentStatus == -1) {
            log.error("VNPay callback checksum signature verification failed!");
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid Checksum Signature"));
        }

        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
        String vnp_BankCode = request.getParameter("vnp_BankCode");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

        Integer orderId = parseOrderId(vnp_TxnRef);
        if (orderId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Order reference is invalid or missing in callback"));
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Order not found with id: " + orderId));
        }

        // Verify amount
        if (!isAmountValid(order, request.getParameter("vnp_Amount"))) {
            log.error("Amount validation failed in callback for order ID: {}", orderId);
            orderService.recordFailedVnpayTransaction(orderId, vnpParams);
            return ResponseEntity.badRequest().body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Invalid payment amount"));
        }

        if (paymentStatus == 1) {
            orderService.recordVnpayTransaction(orderId, vnp_TransactionNo, vnpParams);
            log.info("VNPay payment successful for order ID: {}", orderId);
            PaymentResultResponse result = PaymentResultResponse.builder()
                    .orderId(orderId)
                    .paymentStatus(PaymentStatus.PAID)
                    .transactionNo(vnp_TransactionNo)
                    .amount(order.getTotalAmount())
                    .bankCode(vnp_BankCode)
                    .responseCode(vnp_ResponseCode)
                    .message("Payment successful")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(result, "Payment successful"));
        } else {
            log.warn("VNPay payment failed or cancelled for order ID: {}, status: {}", orderId, paymentStatus);
            orderService.recordFailedVnpayTransaction(orderId, vnpParams);
            PaymentResultResponse result = PaymentResultResponse.builder()
                    .orderId(orderId)
                    .paymentStatus(PaymentStatus.FAILED)
                    .transactionNo(vnp_TransactionNo)
                    .amount(order.getTotalAmount())
                    .bankCode(vnp_BankCode)
                    .responseCode(vnp_ResponseCode)
                    .message("Payment failed or cancelled by user")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(result, "Payment failed or cancelled"));
        }
    }

    /**
     * Server-to-server IPN (Instant Payment Notification): VNPay calls this webhook directly
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> paymentIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        Map<String, String> vnpParams = extractParams(request);

        // 1. Verify Checksum Signature
        int paymentStatus = vnpayService.orderReturn(request);
        if (paymentStatus == -1) {
            log.error("VNPay IPN signature checksum verification failed!");
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return ResponseEntity.ok(response);
        }

        // 2. Parse Order ID
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        Integer orderId = parseOrderId(vnp_TxnRef);
        if (orderId == null) {
            response.put("RspCode", "01");
            response.put("Message", "Order not Found");
            return ResponseEntity.ok(response);
        }

        // 3. Process Transaction with Idempotency and Amount Check
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return ResponseEntity.ok(response);
            }

            // 4. Verify Amount
            if (!isAmountValid(order, request.getParameter("vnp_Amount"))) {
                log.error("VNPay IPN amount mismatch for order ID: {}", orderId);
                orderService.recordFailedVnpayTransaction(orderId, vnpParams);
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return ResponseEntity.ok(response);
            }

            // 5. Check if already confirmed/paid (Idempotency)
            if (order.getPaymentStatus() == PaymentStatus.PAID || order.getVnpTransactionNo() != null) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return ResponseEntity.ok(response);
            }

            if (paymentStatus == 1) {
                String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
                orderService.recordVnpayTransaction(orderId, vnp_TransactionNo, vnpParams);
                log.info("VNPay IPN recorded payment successfully for order ID: {}", orderId);
            } else {
                orderService.recordFailedVnpayTransaction(orderId, vnpParams);
                log.warn("VNPay IPN recorded failed payment for order ID: {}", orderId);
            }

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing VNPay IPN for order: " + orderId, e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return ResponseEntity.ok(response);
        }
    }

    private boolean isAmountValid(Order order, String vnpAmountParam) {
        if (vnpAmountParam == null) return false;
        try {
            long receivedAmount = Long.parseLong(vnpAmountParam) / 100;
            long expectedAmount = "USD".equalsIgnoreCase(vnpayCurrency)
                    ? order.getTotalAmount().longValue()
                    : order.getTotalAmount().multiply(EXCHANGE_RATE).longValue();
            return Math.abs(receivedAmount - expectedAmount) <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Integer parseOrderId(String vnp_TxnRef) {
        if (vnp_TxnRef == null || vnp_TxnRef.isEmpty()) {
            return null;
        }
        try {
            String[] parts = vnp_TxnRef.split("_");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            try {
                return Integer.parseInt(vnp_TxnRef);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }
}