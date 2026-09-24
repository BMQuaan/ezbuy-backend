package com.ezbuy.ezbuy.exceptions;

import io.jsonwebtoken.ExpiredJwtException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ezbuy.ezbuy.dtos.response.ApiResponse;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .data(errors)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password.");
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.FORBIDDEN.value(),
                "You do not have permission to access this resource.");
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.FORBIDDEN.value(), "User is disabled.");
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Token is expired.");
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message;
        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "The name or another unique field already exists.";
        } else if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            message = ex.getMessage();
        }
        else {
            message = "Data integrity violation.";
        }
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientStockException(InsufficientStockException ex) {
        Map<String, Object> details = new HashMap<>();
        if (ex.getProductId() != null) {
            details.put("productId", ex.getProductId());
            details.put("productName", ex.getProductName());
            details.put("requestedQuantity", ex.getRequestedQuantity());
            details.put("availableQuantity", ex.getAvailableQuantity());
        }

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .data(details.isEmpty() ? null : details)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        ApiResponse<Void> apiResponse = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.");
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}