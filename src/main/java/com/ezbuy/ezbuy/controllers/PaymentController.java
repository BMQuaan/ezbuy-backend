package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.entities.Order;
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

import java.net.URI;
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

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Client-side return URL: VNPay redirects user's browser back to this endpoint
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> paymentCallback(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);

        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");

        Integer orderId = parseOrderId(vnp_TxnRef);

        if (orderId == null) {
            return ResponseEntity.badRequest().body("Order reference is invalid or missing in callback");
        }

        String targetUrl = frontendUrl + "/profile/purchasehistory/" + orderId;

        if (paymentStatus == 1) {
            orderService.recordVnpayTransaction(orderId, vnp_TransactionNo);
            log.info("VNPay payment successful for order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetUrl + "?paymentStatus=success"))
                    .build();
        } else {
            log.warn("VNPay payment failed or cancelled for order ID: {}, status: {}", orderId, paymentStatus);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(targetUrl + "?paymentStatus=failed"))
                    .build();
        }
    }

    /**
     * Server-to-server IPN (Instant Payment Notification): VNPay calls this webhook directly
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> paymentIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

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

        // 3. Process Transaction with Idempotency
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return ResponseEntity.ok(response);
            }

            // Check if already paid/confirmed
            if (order.getVnpTransactionNo() != null) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return ResponseEntity.ok(response);
            }

            if (paymentStatus == 1) {
                String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
                orderService.recordVnpayTransaction(orderId, vnp_TransactionNo);
                log.info("VNPay IPN recorded payment for order ID: {}", orderId);
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
}