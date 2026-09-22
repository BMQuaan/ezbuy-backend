package com.ezbuy.ezbuy.enums;

public enum StockStrategyType {
    NAIVE,
    PESSIMISTIC,
    ATOMIC_SQL,
    REDIS_LUA;

    public static StockStrategyType fromString(String value, StockStrategyType defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return StockStrategyType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
