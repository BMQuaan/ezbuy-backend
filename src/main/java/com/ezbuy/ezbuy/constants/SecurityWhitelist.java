package com.ezbuy.ezbuy.constants;


public final class SecurityWhitelist {
    private SecurityWhitelist() {}

    public static final String[] AUTH_WHITELIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/verify-otp",
            "/api/auth/reset-password",
            "/api/search/by-image"
    };

    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/products/**",
            "/api/categories/**",
            "/api/promotions/**",
            "/api/promotions/check",
            "/api/manufacturers",
            "/api/payments/vnpay-callback"
    };
}
