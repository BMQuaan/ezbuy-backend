package com.ezbuy.ezbuy.controllers;

import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.enums.StockStrategyType;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.strategies.stock.StockDeductionContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller dedicated to concurrency testing and load benchmarking with k6 / JMeter.
 * Provides direct endpoints to test stock deduction strategies and reset stock counts.
 */
@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
@Slf4j
public class BenchmarkController {

    private final StockDeductionContext stockDeductionContext;
    private final ProductRepository productRepository;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Deducts stock for a product using the specified strategy.
     * Strategy can be specified via:
     * 1. Header 'X-Stock-Strategy' (e.g. NAIVE, PESSIMISTIC, ATOMIC_SQL, REDIS_LUA)
     * 2. Query param 'strategy'
     * 3. Defaults to application configuration
     */
    @PostMapping("/stock-deduction")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deductStock(
            @RequestParam(defaultValue = "2") Integer productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String strategy,
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();

        StockStrategyType strategyType = strategy != null
                ? StockStrategyType.fromString(strategy, stockDeductionContext.getDefaultStrategyType())
                : stockDeductionContext.resolveStrategyType(request);

        int remainingStock = stockDeductionContext.deduct(strategyType, productId, quantity);
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("strategy", strategyType.name());
        data.put("requestedQuantity", quantity);
        data.put("remainingStock", remainingStock);
        data.put("executionTimeMs", duration);

        return ResponseEntity.ok(ApiResponse.success(data, "Stock deducted successfully"));
    }

    /**
     * Resets stock of a product in both MySQL and Redis for benchmark preparation.
     */
    @PostMapping("/reset-stock")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetStock(
            @RequestParam(defaultValue = "2") Integer productId,
            @RequestParam(defaultValue = "10") int stock
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        product.setQuantityInStock(stock);
        productRepository.save(product);

        // Also sync to Redis
        try {
            String redisKey = "stock:product:" + productId;
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(stock));
            log.info("[BENCHMARK] Reset stock for product {} to {} in DB and Redis", productId, stock);
        } catch (Exception e) {
            log.warn("[BENCHMARK] Redis not reachable while resetting stock: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("stock", stock);

        return ResponseEntity.ok(ApiResponse.success(result, "Stock reset successfully"));
    }

    /**
     * Inspects current stock in both MySQL and Redis.
     */
    @GetMapping("/stock/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStock(@PathVariable Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        String redisStock = null;
        try {
            redisStock = stringRedisTemplate.opsForValue().get("stock:product:" + productId);
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("mysqlStock", product.getQuantityInStock());
        result.put("redisStock", redisStock != null ? Integer.parseInt(redisStock) : "Not cached");

        return ResponseEntity.ok(ApiResponse.success(result, "Stock retrieved"));
    }
}
