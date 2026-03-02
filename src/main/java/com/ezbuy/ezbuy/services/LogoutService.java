package com.ezbuy.ezbuy.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import com.ezbuy.ezbuy.repositories.TokenRepository;
import java.util.Arrays; 

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            var storedToken = tokenRepository.findByToken(refreshToken).orElse(null);
            if (storedToken != null) {
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
            }
        }

        clearRefreshTokenCookie(response);
    }


    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie("refresh_token", null); 
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); 
        response.addCookie(refreshCookie);
    }
}