package com.ezbuy.ezbuy.dtos.request;

import com.ezbuy.ezbuy.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "New status is required")
    private OrderStatus status;
}