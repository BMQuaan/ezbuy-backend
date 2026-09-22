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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pessimistic Locking Strategy (SELECT ... FOR UPDATE).
 * Locks the database row exclusively during the transaction, preventing concurrent reads/writes.
 * Guaranteed 0 overselling.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PessimisticStockStrategy implements StockDeductionStrategy {

    private final ProductRepository productRepository;

    @Override
    public StockStrategyType getType() {
        return StockStrategyType.PESSIMISTIC;
    }

    @Override
    @Transactional
    public int deduct(Integer productId, int quantity) {
        Product product = productRepository.findAndLockById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        if (product.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(productId, product.getName(), quantity, product.getQuantityInStock());
        }

        int newStock = product.getQuantityInStock() - quantity;
        product.setQuantityInStock(newStock);
        productRepository.save(product);

        log.debug("[PESSIMISTIC] Locked & deducted {} for product {}. New stock: {}", quantity, productId, newStock);
        return newStock;
    }

    @Override
    @Transactional
    public void deductStock(List<OrderItem> orderItems) {
        // Sort IDs to prevent deadlock between concurrent multi-item transactions
        List<Integer> sortedProductIds = orderItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Product> lockedProducts = productRepository.findAndLockByIds(sortedProductIds);
        Map<Integer, Product> productMap = lockedProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> productsToSave = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Integer pId = item.getProduct().getId();
            Product product = productMap.get(pId);
            if (product == null) {
                throw new NotFoundException("Product not found with id: " + pId);
            }

            if (product.getQuantityInStock() < item.getQuantity()) {
                throw new InsufficientStockException(pId, product.getName(), item.getQuantity(), product.getQuantityInStock());
            }

            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
            productsToSave.add(product);
        }

        productRepository.saveAll(productsToSave);
    }

    @Override
    @Transactional
    public int restore(Integer productId, int quantity) {
        Product product = productRepository.findAndLockById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        int newStock = product.getQuantityInStock() + quantity;
        product.setQuantityInStock(newStock);
        productRepository.save(product);
        return newStock;
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderItem> orderItems) {
        List<Integer> sortedProductIds = orderItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Product> lockedProducts = productRepository.findAndLockByIds(sortedProductIds);
        Map<Integer, Product> productMap = lockedProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> productsToSave = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProduct().getId());
            if (product != null) {
                product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
                productsToSave.add(product);
            }
        }
        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);
        }
    }
}
