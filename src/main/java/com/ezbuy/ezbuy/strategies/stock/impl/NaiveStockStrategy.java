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

import java.util.ArrayList;
import java.util.List;

/**
 * Baseline Naive Strategy (Un-locked Read-Modify-Write).
 * Loads entity without lock, calculates in Java memory, and saves back to DB.
 * Susceptible to Lost Updates and Overselling under concurrent load.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NaiveStockStrategy implements StockDeductionStrategy {

    private final ProductRepository productRepository;

    @Override
    public StockStrategyType getType() {
        return StockStrategyType.NAIVE;
    }

    @Override
    @Transactional
    public int deduct(Integer productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        if (product.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(productId, product.getName(), quantity, product.getQuantityInStock());
        }

        int newStock = product.getQuantityInStock() - quantity;
        product.setQuantityInStock(newStock);
        productRepository.save(product);

        log.debug("[NAIVE] Deducted {} for product {}. New stock: {}", quantity, productId, newStock);
        return newStock;
    }

    @Override
    @Transactional
    public void deductStock(List<OrderItem> orderItems) {
        List<Product> productsToUpdate = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + item.getProduct().getId()));

            if (product.getQuantityInStock() < item.getQuantity()) {
                throw new InsufficientStockException(product.getId(), product.getName(), item.getQuantity(), product.getQuantityInStock());
            }

            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
            productsToUpdate.add(product);
        }
        productRepository.saveAll(productsToUpdate);
    }

    @Override
    @Transactional
    public int restore(Integer productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        int newStock = product.getQuantityInStock() + quantity;
        product.setQuantityInStock(newStock);
        productRepository.save(product);
        return newStock;
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderItem> orderItems) {
        List<Product> productsToUpdate = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (product != null) {
                product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
                productsToUpdate.add(product);
            }
        }
        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }
}
