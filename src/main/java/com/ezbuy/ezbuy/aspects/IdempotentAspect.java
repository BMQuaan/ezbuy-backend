package com.ezbuy.ezbuy.aspects;

import com.ezbuy.ezbuy.annotations.Idempotent;
import com.ezbuy.ezbuy.dtos.IdempotencyRecord;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.exceptions.DuplicateRequestException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotentAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = resolveHttpServletRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String userId = resolveUserId();
        String idempotencyKey = extractIdempotencyKey(request, idempotent.header());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return processWithIdempotencyKey(joinPoint, idempotent, userId, idempotencyKey.trim());
        }

        // Fallback: If no Idempotency-Key header is provided, protect against rapid double-clicks
        if (idempotent.lockByUserWhenKeyMissing() && userId != null) {
            return processWithUserLock(joinPoint, idempotent, userId);
        }

        return joinPoint.proceed();
    }

    private Object processWithIdempotencyKey(ProceedingJoinPoint joinPoint,
                                            Idempotent idempotent,
                                            String userId,
                                            String idempotencyKey) throws Throwable {
        String userPrefix = (userId != null) ? "user:" + userId + ":" : "anon:";
        String redisKey = idempotent.keyPrefix() + userPrefix + idempotencyKey;

        try {
            IdempotencyRecord initialRecord = IdempotencyRecord.builder()
                    .status(IdempotencyRecord.STATUS_PROCESSING)
                    .createdAt(System.currentTimeMillis())
                    .build();

            String initialJson = objectMapper.writeValueAsString(initialRecord);
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
                    redisKey,
                    initialJson,
                    idempotent.inFlightSeconds(),
                    TimeUnit.SECONDS
            );

            if (Boolean.FALSE.equals(acquired)) {
                // Key already exists in Redis
                String existingData = stringRedisTemplate.opsForValue().get(redisKey);
                if (existingData != null) {
                    IdempotencyRecord existingRecord = objectMapper.readValue(existingData, IdempotencyRecord.class);
                    if (IdempotencyRecord.STATUS_PROCESSING.equals(existingRecord.getStatus())) {
                        log.warn("[IDEMPOTENCY] Concurrent in-flight request detected for key: {}", redisKey);
                        throw new DuplicateRequestException(
                                "A request with Idempotency-Key '" + idempotencyKey + "' is currently being processed. Please wait."
                        );
                    } else if (IdempotencyRecord.STATUS_COMPLETED.equals(existingRecord.getStatus())) {
                        log.info("[IDEMPOTENCY] Returning cached response for key: {}", redisKey);
                        return reconstructCachedResponse(joinPoint, existingRecord);
                    }
                }
            }
        } catch (DuplicateRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[IDEMPOTENCY] Redis is unavailable ({}), bypassing idempotency check and executing request.", e.getMessage());
            return joinPoint.proceed();
        }

        // Key was successfully locked as PROCESSING
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // Remove in-flight lock on failure so the user can fix errors and retry
            safeDeleteRedisKey(redisKey);
            throw ex;
        }

        // Cache the successful response
        try {
            int statusCode = 200;
            String bodyJson = null;

            if (result instanceof ResponseEntity<?> responseEntity) {
                statusCode = responseEntity.getStatusCode().value();
                if (responseEntity.getBody() != null) {
                    bodyJson = objectMapper.writeValueAsString(responseEntity.getBody());
                }
            } else if (result != null) {
                bodyJson = objectMapper.writeValueAsString(result);
            }

            IdempotencyRecord completedRecord = IdempotencyRecord.builder()
                    .status(IdempotencyRecord.STATUS_COMPLETED)
                    .statusCode(statusCode)
                    .responseBody(bodyJson)
                    .createdAt(System.currentTimeMillis())
                    .build();

            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    objectMapper.writeValueAsString(completedRecord),
                    idempotent.expireSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("[IDEMPOTENCY] Failed to save completed response to Redis for key: {}", redisKey, e);
        }

        return result;
    }

    private Object processWithUserLock(ProceedingJoinPoint joinPoint,
                                       Idempotent idempotent,
                                       String userId) throws Throwable {
        String lockKey = "lock:order:create:user:" + userId;
        boolean lockAcquired = false;

        try {
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    "LOCKED",
                    idempotent.lockSecondsWhenKeyMissing(),
                    TimeUnit.SECONDS
            );
            lockAcquired = Boolean.TRUE.equals(acquired);

            if (!lockAcquired) {
                log.warn("[IDEMPOTENCY] User double-click detected for user ID: {}", userId);
                throw new DuplicateRequestException(
                        "Another order is currently being processed for your account. Please wait a few seconds before trying again."
                );
            }
        } catch (DuplicateRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[IDEMPOTENCY] Redis unavailable during user lock check: {}", e.getMessage());
            return joinPoint.proceed();
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (lockAcquired) {
                safeDeleteRedisKey(lockKey);
            }
        }
    }

    private Object reconstructCachedResponse(ProceedingJoinPoint joinPoint, IdempotencyRecord record) throws Exception {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (ResponseEntity.class.isAssignableFrom(returnType)) {
            Method method = signature.getMethod();
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType pt) {
                Type bodyType = pt.getActualTypeArguments()[0];
                JavaType javaType = objectMapper.constructType(bodyType);
                Object body = objectMapper.readValue(record.getResponseBody(), javaType);
                return ResponseEntity.status(record.getStatusCode()).body(body);
            }
            Object body = objectMapper.readValue(record.getResponseBody(), Object.class);
            return ResponseEntity.status(record.getStatusCode()).body(body);
        }

        return objectMapper.readValue(record.getResponseBody(), returnType);
    }

    private HttpServletRequest resolveHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getRequest() : null;
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user && user.getId() != null) {
            return String.valueOf(user.getId());
        }

        if (auth.getName() != null && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return auth.getName();
        }

        return null;
    }

    private String extractIdempotencyKey(HttpServletRequest request, String configuredHeader) {
        String key = request.getHeader(configuredHeader);
        if (key != null && !key.isBlank()) {
            return key;
        }
        String fallbackKey = request.getHeader("X-Idempotency-Key");
        if (fallbackKey != null && !fallbackKey.isBlank()) {
            return fallbackKey;
        }
        return null;
    }

    private void safeDeleteRedisKey(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("[IDEMPOTENCY] Failed to delete Redis key: {}", key, e);
        }
    }
}
