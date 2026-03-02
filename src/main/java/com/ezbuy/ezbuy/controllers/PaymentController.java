package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.services.OrderService;
import com.ezbuy.ezbuy.services.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnpayService;
    private final OrderService orderService;


    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> paymentCallback(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);
        
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
        
        Integer orderId = null;

        if (vnp_TxnRef != null && !vnp_TxnRef.isEmpty()) {
            try {
                String[] parts = vnp_TxnRef.split("_");
                orderId = Integer.parseInt(parts[0]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                try {
                    orderId = Integer.parseInt(vnp_TxnRef);
                } catch (NumberFormatException ex) {
                    return ResponseEntity.badRequest().body("Invalid Transaction Reference Code");
                }
            }
        }

        String frontendUrl = "http://localhost:3000/orders/" + orderId;

        if (orderId != null) {
            if (paymentStatus == 1) {
                orderService.recordVnpayTransaction(orderId, vnp_TransactionNo);
                
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "?status=success"))
                    .build();
            } else {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "?status=failed"))
                    .build();
            }
        }
        
        return ResponseEntity.badRequest().body("Order information not found in callback");
    }
}