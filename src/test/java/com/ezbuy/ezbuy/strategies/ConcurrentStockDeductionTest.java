package com.ezbuy.ezbuy.strategies;

import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.exceptions.InsufficientStockException;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.strategies.stock.impl.AtomicSqlStockStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentStockDeductionTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("Simulate 50 concurrent threads: Atomic SQL guarantees zero overselling")
    void concurrentDeduction_GuaranteesZeroOverselling() throws InterruptedException {
        int initialStock = 10;
        int totalThreads = 50;
        AtomicInteger simulatedDbStock = new AtomicInteger(initialStock);

        AtomicSqlStockStrategy strategy = new AtomicSqlStockStrategy(productRepository);

        Product product = Product.builder()
                .id(2)
                .name("iPhone 17")
                .price(BigDecimal.valueOf(1000))
                .quantityInStock(initialStock)
                .build();

        // Simulate atomic DB behavior: UPDATE products SET stock = stock - 1 WHERE id = 2 AND stock >= 1
        when(productRepository.deductStockAtomic(2, 1)).thenAnswer(invocation -> {
            int current = simulatedDbStock.get();
            while (current >= 1) {
                if (simulatedDbStock.compareAndSet(current, current - 1)) {
                    return 1; // 1 row updated
                }
                current = simulatedDbStock.get();
            }
            return 0; // 0 rows updated (insufficient stock)
        });

        when(productRepository.findById(2)).thenReturn(Optional.of(product));

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for all threads to align
                    strategy.deduct(2, 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected error
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Unleash all 50 threads at the same instant
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Verify: Exactly 10 succeed, 40 fail, remaining simulated stock is 0 (ZERO OVERSELLING)
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(40);
        assertThat(simulatedDbStock.get()).isEqualTo(0);
    }
}
