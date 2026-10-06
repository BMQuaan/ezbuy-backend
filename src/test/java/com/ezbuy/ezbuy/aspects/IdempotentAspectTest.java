package com.ezbuy.ezbuy.aspects;

import com.ezbuy.ezbuy.annotations.Idempotent;
import com.ezbuy.ezbuy.dtos.IdempotencyRecord;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.exceptions.DuplicateRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentAspectTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Idempotent idempotentAnnotation;

    private ObjectMapper objectMapper;
    private IdempotentAspect aspect;
    private MockHttpServletRequest request;
    private User testUser;

    // Dummy method for reflection signature
    public ResponseEntity<ApiResponse<OrderResponse>> sampleMethod() {
        return null;
    }

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        aspect = new IdempotentAspect(stringRedisTemplate, objectMapper);

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        testUser = new User();
        testUser.setId(42);
        testUser.setEmail("test@ezbuy.com");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        lenient().when(idempotentAnnotation.header()).thenReturn("Idempotency-Key");
        lenient().when(idempotentAnnotation.keyPrefix()).thenReturn("idempotency:order:");
        lenient().when(idempotentAnnotation.expireSeconds()).thenReturn(86400L);
        lenient().when(idempotentAnnotation.inFlightSeconds()).thenReturn(120L);
        lenient().when(idempotentAnnotation.lockByUserWhenKeyMissing()).thenReturn(true);
        lenient().when(idempotentAnnotation.lockSecondsWhenKeyMissing()).thenReturn(5L);

        Method method = getClass().getMethod("sampleMethod");
        lenient().when(methodSignature.getMethod()).thenReturn(method);
        lenient().doReturn(ResponseEntity.class).when(methodSignature).getReturnType();
        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Scenario 1: First request with Idempotency-Key executes normally and caches result")
    void testFirstRequest_Success() throws Throwable {
        String key = "req-uuid-123";
        request.addHeader("Idempotency-Key", key);

        when(valueOperations.setIfAbsent(eq("idempotency:order:user:42:" + key), anyString(), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(101)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(250))
                .build();
        ResponseEntity<ApiResponse<OrderResponse>> targetResult =
                new ResponseEntity<>(ApiResponse.success(orderResponse, "Order created"), HttpStatus.CREATED);

        when(joinPoint.proceed()).thenReturn(targetResult);

        Object actualResult = aspect.handleIdempotency(joinPoint, idempotentAnnotation);

        assertThat(actualResult).isEqualTo(targetResult);
        verify(joinPoint, times(1)).proceed();

        // Verify successful completed record is cached in Redis
        verify(valueOperations, times(1)).set(
                eq("idempotency:order:user:42:" + key),
                contains("\"status\":\"COMPLETED\""),
                eq(86400L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Scenario 2: Concurrent request with same Idempotency-Key in flight throws DuplicateRequestException (409)")
    void testConcurrentRequest_ThrowsConflict() throws Throwable {
        String key = "req-uuid-concurrent";
        request.addHeader("Idempotency-Key", key);

        when(valueOperations.setIfAbsent(eq("idempotency:order:user:42:" + key), anyString(), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        IdempotencyRecord inFlightRecord = IdempotencyRecord.builder()
                .status(IdempotencyRecord.STATUS_PROCESSING)
                .createdAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("idempotency:order:user:42:" + key))
                .thenReturn(objectMapper.writeValueAsString(inFlightRecord));

        assertThatThrownBy(() -> aspect.handleIdempotency(joinPoint, idempotentAnnotation))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("currently being processed");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Scenario 3: Subsequent request with completed Idempotency-Key replays cached response")
    void testSubsequentRequest_ReplaysCachedResponse() throws Throwable {
        String key = "req-uuid-completed";
        request.addHeader("Idempotency-Key", key);

        when(valueOperations.setIfAbsent(eq("idempotency:order:user:42:" + key), anyString(), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        OrderResponse cachedOrder = OrderResponse.builder()
                .orderId(202)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(500))
                .build();
        ApiResponse<OrderResponse> cachedApiResp = ApiResponse.success(cachedOrder, "Cached order");

        IdempotencyRecord completedRecord = IdempotencyRecord.builder()
                .status(IdempotencyRecord.STATUS_COMPLETED)
                .statusCode(201)
                .responseBody(objectMapper.writeValueAsString(cachedApiResp))
                .createdAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("idempotency:order:user:42:" + key))
                .thenReturn(objectMapper.writeValueAsString(completedRecord));

        Object result = aspect.handleIdempotency(joinPoint, idempotentAnnotation);

        assertThat(result).isInstanceOf(ResponseEntity.class);
        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<OrderResponse>> responseEntity = (ResponseEntity<ApiResponse<OrderResponse>>) result;
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData().getOrderId()).isEqualTo(202);

        // Core business logic was NOT called again
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Scenario 4: Request fails with exception deletes in-flight Redis lock")
    void testRequestFails_DeletesLock() throws Throwable {
        String key = "req-uuid-fail";
        request.addHeader("Idempotency-Key", key);

        when(valueOperations.setIfAbsent(eq("idempotency:order:user:42:" + key), anyString(), eq(120L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("Cart validation failed"));

        assertThatThrownBy(() -> aspect.handleIdempotency(joinPoint, idempotentAnnotation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cart validation failed");

        // Verify key was cleaned up so retry is possible
        verify(stringRedisTemplate, times(1)).delete("idempotency:order:user:42:" + key);
    }

    @Test
    @DisplayName("Scenario 5: No Idempotency-Key provided applies user double-click lock")
    void testNoHeader_UserLockAcquired() throws Throwable {
        when(valueOperations.setIfAbsent(eq("lock:order:create:user:42"), eq("LOCKED"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.handleIdempotency(joinPoint, idempotentAnnotation);

        assertThat(result).isEqualTo("OK");
        verify(stringRedisTemplate, times(1)).delete("lock:order:create:user:42");
    }

    @Test
    @DisplayName("Scenario 6: No Idempotency-Key and user lock already held throws DuplicateRequestException")
    void testNoHeader_UserLockConflict() throws Throwable {
        when(valueOperations.setIfAbsent(eq("lock:order:create:user:42"), eq("LOCKED"), eq(5L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        assertThatThrownBy(() -> aspect.handleIdempotency(joinPoint, idempotentAnnotation))
                .isInstanceOf(DuplicateRequestException.class)
                .hasMessageContaining("Another order is currently being processed for your account");

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Scenario 7: Redis failure degrades gracefully and proceeds with execution")
    void testRedisUnavailable_GracefulDegradation() throws Throwable {
        String key = "req-uuid-redis-down";
        request.addHeader("Idempotency-Key", key);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenThrow(new RedisConnectionFailureException("Unable to connect to Redis"));

        when(joinPoint.proceed()).thenReturn("FALLBACK_RESULT");

        Object result = aspect.handleIdempotency(joinPoint, idempotentAnnotation);

        assertThat(result).isEqualTo("FALLBACK_RESULT");
        verify(joinPoint, times(1)).proceed();
    }
}
