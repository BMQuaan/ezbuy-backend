package com.ezbuy.ezbuy.strategies.stock;

import com.ezbuy.ezbuy.entities.OrderItem;
import com.ezbuy.ezbuy.enums.StockStrategyType;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Context and Factory for StockDeductionStrategy.
 * Dynamically resolves the strategy to use based on HTTP Header, query parameter, or application config.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockDeductionContext {

    private final List<StockDeductionStrategy> strategyList;
    private final Map<StockStrategyType, StockDeductionStrategy> strategyMap = new EnumMap<>(StockStrategyType.class);

    public static final String HEADER_STOCK_STRATEGY = "X-Stock-Strategy";
    public static final String PARAM_STOCK_STRATEGY = "strategy";

    @Value("${app.stock.default-strategy:ATOMIC_SQL}")
    private String defaultStrategyName;

    private StockStrategyType defaultStrategyType = StockStrategyType.ATOMIC_SQL;

    @PostConstruct
    public void init() {
        for (StockDeductionStrategy strategy : strategyList) {
            strategyMap.put(strategy.getType(), strategy);
            log.info("Registered stock deduction strategy: {}", strategy.getType());
        }
        defaultStrategyType = StockStrategyType.fromString(defaultStrategyName, StockStrategyType.ATOMIC_SQL);
        log.info("Default stock deduction strategy set to: {}", defaultStrategyType);
    }

    /**
     * Resolves the strategy to use based on HttpServletRequest header, param, or default config.
     */
    public StockStrategyType resolveStrategyType(HttpServletRequest request) {
        if (request == null) {
            return defaultStrategyType;
        }

        String strategyHeader = request.getHeader(HEADER_STOCK_STRATEGY);
        if (strategyHeader != null && !strategyHeader.trim().isEmpty()) {
            return StockStrategyType.fromString(strategyHeader, defaultStrategyType);
        }

        String strategyParam = request.getParameter(PARAM_STOCK_STRATEGY);
        if (strategyParam != null && !strategyParam.trim().isEmpty()) {
            return StockStrategyType.fromString(strategyParam, defaultStrategyType);
        }

        return defaultStrategyType;
    }

    /**
     * Retrieves the concrete strategy implementation.
     */
    public StockDeductionStrategy getStrategy(StockStrategyType type) {
        StockDeductionStrategy strategy = strategyMap.get(type != null ? type : defaultStrategyType);
        if (strategy == null) {
            log.warn("Strategy {} not found, falling back to {}", type, defaultStrategyType);
            strategy = strategyMap.get(defaultStrategyType);
        }
        return strategy;
    }

    /**
     * Deducts stock for a single product with specified strategy.
     */
    public int deduct(StockStrategyType type, Integer productId, int quantity) {
        return getStrategy(type).deduct(productId, quantity);
    }

    /**
     * Deducts stock for order items with specified strategy.
     */
    public void deductStock(StockStrategyType type, List<OrderItem> orderItems) {
        getStrategy(type).deductStock(orderItems);
    }

    /**
     * Restores stock for a single product with specified strategy.
     */
    public int restore(StockStrategyType type, Integer productId, int quantity) {
        return getStrategy(type).restore(productId, quantity);
    }

    /**
     * Restores stock for order items with specified strategy.
     */
    public void restoreStock(StockStrategyType type, List<OrderItem> orderItems) {
        getStrategy(type).restoreStock(orderItems);
    }

    public StockStrategyType getDefaultStrategyType() {
        return defaultStrategyType;
    }
}
