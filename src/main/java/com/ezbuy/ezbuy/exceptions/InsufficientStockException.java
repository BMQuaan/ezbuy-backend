package com.ezbuy.ezbuy.exceptions;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final Integer productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.productName = null;
        this.requestedQuantity = 0;
        this.availableQuantity = 0;
    }

    public InsufficientStockException(Integer productId, String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product '%s' (ID: %d). Requested: %d, Available: %d",
                productName != null ? productName : "Unknown",
                productId,
                requestedQuantity,
                availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientStockException(Integer productId, int requestedQuantity) {
        super(String.format("Insufficient stock for product ID: %d. Requested: %d", productId, requestedQuantity));
        this.productId = productId;
        this.productName = null;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = 0;
    }
}
