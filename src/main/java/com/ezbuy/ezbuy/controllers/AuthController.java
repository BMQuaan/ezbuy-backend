package com.ezbuy.ezbuy.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ezbuy.ezbuy.dtos.request.ForgotPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.LoginRequest;
import com.ezbuy.ezbuy.dtos.request.RegisterRequest;
import com.ezbuy.ezbuy.dtos.request.ResetPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.VerifyOtpRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.AuthResponse;
import com.ezbuy.ezbuy.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register( 
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request, response);
        return new ResponseEntity<>(
            ApiResponse.success(authResponse, "Registration successful"), 
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

     @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken( 
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP has been sent to your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP verified successfully."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletResponse response 
    ) {
        AuthResponse authResponse = authService.resetPassword(request, response); 
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Password has been reset successfully."));
    }
}