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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Redis In-Memory Pre-decrement Strategy with Atomic Lua Script:
 * 1. Checks and decrements stock in Redis RAM in sub-millisecond time.
 * 2. If Redis stock >= requested, synchronously syncs deduction to MySQL.
 * 3. If Redis fails or is unavailable, gracefully falls back to Atomic SQL.
 * Highly suitable for Flash Sale events with tens of thousands of concurrent requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLuaStockStrategy implements StockDeductionStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductRepository productRepository;
    private final AtomicSqlStockStrategy atomicSqlStockStrategy;

    private static final String STOCK_KEY_PREFIX = "stock:product:";

    // Lua Script for atomic check-and-decrement
    // Returns:
    // >= 0: Remaining stock after decrement
    //   -1: Insufficient stock
    //   -2: Key does not exist in Redis (needs warm-up from DB)
    private static final String DEDUCT_LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local requested = tonumber(ARGV[1])\n" +
            "local current = redis.call('get', key)\n" +
            "if not current then\n" +
            "    return -2\n" +
            "end\n" +
            "local stock = tonumber(current)\n" +
            "if stock < requested then\n" +
            "    return -1\n" +
            "end\n" +
            "return redis.call('decrby', key, requested)";

    private static final DefaultRedisScript<Long> REDIS_DEDUCT_SCRIPT =
            new DefaultRedisScript<>(DEDUCT_LUA_SCRIPT, Long.class);

    @Override
    public StockStrategyType getType() {
        return StockStrategyType.REDIS_LUA;
    }

    @Override
    @Transactional
    public int deduct(Integer productId, int quantity) {
        String key = STOCK_KEY_PREFIX + productId;

        try {
            Long result = executeLuaDeduct(key, quantity);

            // Key not initialized in Redis -> warm up from DB and retry
            if (result == -2) {
                warmUpStockInRedis(productId);
                result = executeLuaDeduct(key, quantity);
            }

            if (result == -1) {
                String currentStockStr = stringRedisTemplate.opsForValue().get(key);
                int available = currentStockStr != null ? Integer.parseInt(currentStockStr) : 0;
                throw new InsufficientStockException(productId, null, quantity, available);
            }

            // Sync deduction to MySQL
            try {
                productRepository.deductStockAtomic(productId, quantity);
            } catch (Exception e) {
                // Compensate Redis on DB failure
                stringRedisTemplate.opsForValue().increment(key, quantity);
                log.error("[REDIS_LUA] DB deduction failed, compensated Redis stock for product {}", productId, e);
                throw e;
            }

            log.debug("[REDIS_LUA] Pre-decremented in Redis and synced to DB. Remaining: {}", result);
            return result.intValue();

        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[REDIS_LUA] Redis error or unavailable. Falling back to AtomicSqlStockStrategy: {}", e.getMessage());
            return atomicSqlStockStrategy.deduct(productId, quantity);
        }
    }

    @Override
    @Transactional
    public void deductStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            deduct(item.getProduct().getId(), item.getQuantity());
        }
    }

    @Override
    @Transactional
    public int restore(Integer productId, int quantity) {
        String key = STOCK_KEY_PREFIX + productId;
        try {
            stringRedisTemplate.opsForValue().increment(key, quantity);
        } catch (Exception e) {
            log.warn("[REDIS_LUA] Failed to increment Redis stock for product {}: {}", productId, e.getMessage());
        }
        return atomicSqlStockStrategy.restore(productId, quantity);
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            restore(item.getProduct().getId(), item.getQuantity());
        }
    }

    /**
     * Warms up Redis stock cache from current MySQL database.
     */
    public void warmUpStockInRedis(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
        String key = STOCK_KEY_PREFIX + productId;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(product.getQuantityInStock()));
        log.info("[REDIS_LUA] Warmed up stock in Redis for product {}: {}", productId, product.getQuantityInStock());
    }

    private Long executeLuaDeduct(String key, int quantity) {
        return stringRedisTemplate.execute(
                REDIS_DEDUCT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(quantity)
        );
    }
}
