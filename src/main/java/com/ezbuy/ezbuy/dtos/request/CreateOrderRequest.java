package com.ezbuy.ezbuy.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String note;

    @NotNull(message = "Payment method ID is required")
    private Integer paymentId;

    private String promoCode;
}