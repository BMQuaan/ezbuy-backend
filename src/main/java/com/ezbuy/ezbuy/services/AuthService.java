package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.ForgotPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.LoginRequest;
import com.ezbuy.ezbuy.dtos.request.RegisterRequest;
import com.ezbuy.ezbuy.dtos.request.ResetPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.VerifyOtpRequest;
import com.ezbuy.ezbuy.dtos.response.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request, HttpServletResponse response);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(ForgotPasswordRequest request);
    
    void verifyOtp(VerifyOtpRequest request); 
    
    AuthResponse resetPassword(ResetPasswordRequest request, HttpServletResponse response);
}