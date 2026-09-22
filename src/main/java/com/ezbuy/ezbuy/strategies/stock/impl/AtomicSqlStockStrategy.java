package com.ezbuy.ezbuy.strategies.stock.impl;

import com.ezbuy.ezbuy.entities.OrderItem;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.enums.StockStrategyType;
import com.ezbuy.ezbuy.exceptions.InsufficientStockException;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.strategies.stock.StockDeductionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Atomic SQL Update Strategy:
 * UPDATE products SET quantity_in_stock = quantity_in_stock - :qty WHERE id = :id AND quantity_in_stock >= :qty
 * Uses InnoDB's row-level lock directly during the single UPDATE execution without holding application locks.
 * Extremely efficient with zero overselling.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AtomicSqlStockStrategy implements StockDeductionStrategy {

    private final ProductRepository productRepository;

    @Override
    public StockStrategyType getType() {
        return StockStrategyType.ATOMIC_SQL;
    }

    @Override
    @Transactional
    public int deduct(Integer productId, int quantity) {
        int updatedRows = productRepository.deductStockAtomic(productId, quantity);
        if (updatedRows == 0) {
            // Check if product exists or stock was insufficient
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
            throw new InsufficientStockException(productId, product.getName(), quantity, product.getQuantityInStock());
        }

        Product product = productRepository.findById(productId).orElse(null);
        int remaining = product != null ? product.getQuantityInStock() : 0;
        log.debug("[ATOMIC_SQL] Deducted {} for product {}. Remaining stock: {}", quantity, productId, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public void deductStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Integer productId = item.getProduct().getId();
            int requestedQty = item.getQuantity();

            int updatedRows = productRepository.deductStockAtomic(productId, requestedQty);
            if (updatedRows == 0) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
                throw new InsufficientStockException(productId, product.getName(), requestedQty, product.getQuantityInStock());
            }
        }
    }

    @Override
    @Transactional
    public int restore(Integer productId, int quantity) {
        int updatedRows = productRepository.restoreStockAtomic(productId, quantity);
        if (updatedRows == 0) {
            throw new NotFoundException("Product not found with id: " + productId);
        }
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getQuantityInStock() : 0;
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            productRepository.restoreStockAtomic(item.getProduct().getId(), item.getQuantity());
        }
    }
}
