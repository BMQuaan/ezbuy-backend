package com.ezbuy.ezbuy.dtos.response;

import com.ezbuy.ezbuy.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultResponse {
    private Integer orderId;
    private PaymentStatus paymentStatus;
    private String transactionNo;
    private BigDecimal amount;
    private String bankCode;
    private String responseCode;
    private String message;
}
