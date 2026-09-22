package com.ezbuy.ezbuy.strategies.stock;

import com.ezbuy.ezbuy.entities.OrderItem;
import com.ezbuy.ezbuy.enums.StockStrategyType;

import java.util.List;

public interface StockDeductionStrategy {

    /**
     * Returns the strategy type identifier.
     */
    StockStrategyType getType();

    /**
     * Deducts stock for a single product.
     *
     * @param productId product ID
     * @param quantity  quantity to deduct
     * @return remaining stock after deduction
     * @throws com.ezbuy.ezbuy.exceptions.InsufficientStockException if stock is insufficient
     */
    int deduct(Integer productId, int quantity);

    /**
     * Deducts stock for multiple order items within a single order transaction.
     *
     * @param orderItems list of order items to deduct stock for
     * @throws com.ezbuy.ezbuy.exceptions.InsufficientStockException if any product has insufficient stock
     */
    void deductStock(List<OrderItem> orderItems);

    /**
     * Restores stock for a single product (e.g. upon cancellation or rollback).
     *
     * @param productId product ID
     * @param quantity  quantity to restore
     * @return remaining stock after restoration
     */
    int restore(Integer productId, int quantity);

    /**
     * Restores stock for multiple order items (e.g. upon order cancellation).
     *
     * @param orderItems list of order items to restore stock for
     */
    void restoreStock(List<OrderItem> orderItems);
}
