package com.ezbuy.ezbuy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce idempotency on controller endpoints.
 * - If Idempotency-Key header is present:
 *     1. Validates key state in Redis.
 *     2. Returns HTTP 409 Conflict if another identical request is currently IN_FLIGHT.
 *     3. Replays cached response directly if already COMPLETED.
 *     4. Removes lock if execution fails with an exception so client can retry.
 * - If Idempotency-Key header is absent:
 *     Can optionally acquire a short-lived user-level distributed lock to prevent rapid double-clicks.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * HTTP Header name to extract the idempotency key from.
     */
    String header() default "Idempotency-Key";

    /**
     * Prefix for Redis cache keys.
     */
    String keyPrefix() default "idempotency:order:";

    /**
     * Retention period in seconds for cached successful responses (default: 24 hours).
     */
    long expireSeconds() default 86400;

    /**
     * Expiration period in seconds for in-flight request lock (default: 120 seconds).
     */
    long inFlightSeconds() default 120;

    /**
     * Whether to lock by User ID to prevent concurrent double-click submissions when key header is missing.
     */
    boolean lockByUserWhenKeyMissing() default true;

    /**
     * Duration in seconds for the user double-click lock when key header is missing (default: 5 seconds).
     */
    long lockSecondsWhenKeyMissing() default 5;
}
