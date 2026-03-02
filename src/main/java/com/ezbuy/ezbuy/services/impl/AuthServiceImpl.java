package com.ezbuy.ezbuy.services.impl;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezbuy.ezbuy.dtos.request.ForgotPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.LoginRequest;
import com.ezbuy.ezbuy.dtos.request.RegisterRequest;
import com.ezbuy.ezbuy.dtos.request.ResetPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.VerifyOtpRequest;
import com.ezbuy.ezbuy.dtos.response.AuthResponse;
import com.ezbuy.ezbuy.entities.ForgotPassword;
import com.ezbuy.ezbuy.entities.Role;
import com.ezbuy.ezbuy.entities.Token;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.enums.LoginType;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.repositories.ForgotPasswordRepository;
import com.ezbuy.ezbuy.repositories.RoleRepository;
import com.ezbuy.ezbuy.repositories.TokenRepository;
import com.ezbuy.ezbuy.repositories.UserRepository;
import com.ezbuy.ezbuy.services.AuthService;
import com.ezbuy.ezbuy.services.EmailService;
import com.ezbuy.ezbuy.services.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private int refreshTokenExpirationDays = 7;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use.");
        });

        Role userRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Role 'CUSTOMER' not found in database."));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .loginType(LoginType.LOCAL)
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(user);

        return generateAndHandleTokens(savedUser, response);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal(); 

        return generateAndHandleTokens(user, response);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new IllegalArgumentException("No cookies found in request.");
        }
        final String refreshToken = Arrays.stream(cookies)
                                        .filter(cookie -> "refresh_token".equals(cookie.getName()))
                                        .findFirst()
                                        .map(Cookie::getValue)
                                        .orElseThrow(() -> new IllegalArgumentException("Refresh token is missing from cookies."));
        
        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Refresh token is expired.");
        }

        User user = this.userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!user.isActive()) {
            throw new IllegalStateException("User account is deactivated.");
        }

        tokenRepository.findByToken(refreshToken)
                .filter(t -> !t.isRevoked())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is revoked or does not exist."));
        
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Refresh token is invalid.");
        }

        return generateAndHandleTokens(user, response);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

        String otp = new SecureRandom().ints(0, 10)
                                       .limit(6)
                                       .mapToObj(String::valueOf)
                                       .collect(Collectors.joining());

        ForgotPassword fp = ForgotPassword.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(10)) 
                .used(false)
                .build();
        forgotPasswordRepository.save(fp);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send OTP email, please try again.");
        }
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found."));

        ForgotPassword fp = findValidOtpForUser(user);

        if (!passwordEncoder.matches(request.getOtp(), fp.getOtpHash())) {
            throw new IllegalStateException("Invalid OTP.");
        }
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found."));

        ForgotPassword fp = findValidOtpForUser(user);
        if (!passwordEncoder.matches(request.getOtp(), fp.getOtpHash())) {
            throw new IllegalStateException("Invalid OTP.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        fp.setUsed(true);
        forgotPasswordRepository.save(fp);

        return generateAndHandleTokens(user, response);
    }
    
    private ForgotPassword findValidOtpForUser(User user) {
        ForgotPassword fp = forgotPasswordRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException("No valid OTP found. Please request a new one."));

        if (fp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP has expired. Please request a new one.");
        }
        
        return fp;
    }

    private AuthResponse generateAndHandleTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        
        saveRefreshToken(user, refreshToken);

        setRefreshTokenCookie(response, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .fullName(user.getFirstName() + " " + user.getLastName())
                .imageUrl(user.getUserAvatar())
                .role(user.getRole().getName()) 
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findByUserAndRevokedFalse(user);
        if (validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(validUserTokens);
    }
    
    private void saveRefreshToken(User user, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        Token token = Token.builder()
                .user(user)
                .token(refreshToken)
                .revoked(false)
                .expiresAt(expiresAt) 
                .build();
        tokenRepository.save(token);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); 
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenExpirationDays * 24 * 60 * 60); 
        response.addCookie(refreshTokenCookie);
    }
}