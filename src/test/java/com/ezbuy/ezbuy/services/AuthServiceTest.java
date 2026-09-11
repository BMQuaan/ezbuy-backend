package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.ForgotPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.LoginRequest;
import com.ezbuy.ezbuy.dtos.request.RegisterRequest;
import com.ezbuy.ezbuy.dtos.request.ResetPasswordRequest;
import com.ezbuy.ezbuy.dtos.request.VerifyOtpRequest;
import com.ezbuy.ezbuy.dtos.response.AuthResponse;
import com.ezbuy.ezbuy.entities.ForgotPassword;
import com.ezbuy.ezbuy.entities.Role;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.enums.LoginType;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.repositories.ForgotPasswordRepository;
import com.ezbuy.ezbuy.repositories.RoleRepository;
import com.ezbuy.ezbuy.repositories.TokenRepository;
import com.ezbuy.ezbuy.repositories.UserRepository;
import com.ezbuy.ezbuy.services.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private ForgotPasswordRepository forgotPasswordRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .id(1)
                .name("CUSTOMER")
                .build();

        sampleUser = User.builder()
                .id(1)
                .firstName("Nguyen")
                .lastName("Van A")
                .email("user@example.com")
                .password("encoded_password")
                .role(customerRole)
                .loginType(LoginType.LOCAL)
                .isActive(true)
                .build();
    }

    private RegisterRequest createRegisterRequest(String password, String confirmPassword) {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Nguyen");
        req.setLastName("Van A");
        req.setEmail("user@example.com");
        req.setPassword(password);
        req.setConfirmPassword(confirmPassword);
        return req;
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Register successful -> creates user and returns AuthResponse")
        void register_Success() {
            RegisterRequest request = createRegisterRequest("Password123", "Password123");

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("sample_access_token");
            when(jwtService.generateRefreshToken(sampleUser)).thenReturn("sample_refresh_token");
            when(tokenRepository.findByUserAndRevokedFalse(sampleUser)).thenReturn(Collections.emptyList());

            AuthResponse response = authService.register(request, httpServletResponse);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("sample_access_token");
            assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");

            verify(userRepository).save(any(User.class));
            verify(httpServletResponse).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("Register fails when passwords do not match")
        void register_PasswordMismatch_ThrowsException() {
            RegisterRequest request = createRegisterRequest("Password123", "DifferentPassword");

            assertThatThrownBy(() -> authService.register(request, httpServletResponse))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Passwords do not match.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Register fails when email is already in use")
        void register_DuplicateEmail_ThrowsException() {
            RegisterRequest request = createRegisterRequest("Password123", "Password123");

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> authService.register(request, httpServletResponse))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already in use.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Register fails when CUSTOMER role is missing in database")
        void register_RoleNotFound_ThrowsException() {
            RegisterRequest request = createRegisterRequest("Password123", "Password123");

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(request, httpServletResponse))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Role 'CUSTOMER' not found in database.");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Login successful -> returns AuthResponse with tokens")
        void login_Success() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("Password123");

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(sampleUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(jwtService.generateAccessToken(sampleUser)).thenReturn("sample_access_token");
            when(jwtService.generateRefreshToken(sampleUser)).thenReturn("sample_refresh_token");
            when(tokenRepository.findByUserAndRevokedFalse(sampleUser)).thenReturn(Collections.emptyList());

            AuthResponse response = authService.login(request, httpServletResponse);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("sample_access_token");
            assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
            verify(httpServletResponse).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("Login fails with bad credentials")
        void login_BadCredentials_ThrowsException() {
            LoginRequest request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("WrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid email or password."));

            assertThatThrownBy(() -> authService.login(request, httpServletResponse))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password.");
        }
    }

    @Nested
    @DisplayName("OTP & Password Reset Tests")
    class OtpAndResetPasswordTests {

        @Test
        @DisplayName("Forgot password -> generates OTP, saves hash and sends email")
        void forgotPassword_Success() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("user@example.com");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_otp");

            authService.forgotPassword(request);

            verify(forgotPasswordRepository).save(any(ForgotPassword.class));
            verify(emailService).sendOtpEmail(eq("user@example.com"), anyString());
        }

        @Test
        @DisplayName("Verify OTP successful when OTP matches and is not expired")
        void verifyOtp_Success() {
            VerifyOtpRequest request = new VerifyOtpRequest();
            request.setEmail("user@example.com");
            request.setOtp("123456");

            ForgotPassword fp = ForgotPassword.builder()
                    .user(sampleUser)
                    .otpHash("hashed_123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(false)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
            when(forgotPasswordRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(sampleUser))
                    .thenReturn(Optional.of(fp));
            when(passwordEncoder.matches("123456", "hashed_123456")).thenReturn(true);

            authService.verifyOtp(request);
        }

        @Test
        @DisplayName("Verify OTP fails when OTP is invalid")
        void verifyOtp_InvalidOtp_ThrowsException() {
            VerifyOtpRequest request = new VerifyOtpRequest();
            request.setEmail("user@example.com");
            request.setOtp("999999");

            ForgotPassword fp = ForgotPassword.builder()
                    .user(sampleUser)
                    .otpHash("hashed_123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(false)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
            when(forgotPasswordRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(sampleUser))
                    .thenReturn(Optional.of(fp));
            when(passwordEncoder.matches("999999", "hashed_123456")).thenReturn(false);

            assertThatThrownBy(() -> authService.verifyOtp(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid OTP.");
        }

        @Test
        @DisplayName("Verify OTP fails when OTP is expired")
        void verifyOtp_ExpiredOtp_ThrowsException() {
            VerifyOtpRequest request = new VerifyOtpRequest();
            request.setEmail("user@example.com");
            request.setOtp("123456");

            ForgotPassword fp = ForgotPassword.builder()
                    .user(sampleUser)
                    .otpHash("hashed_123456")
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .used(false)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
            when(forgotPasswordRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(sampleUser))
                    .thenReturn(Optional.of(fp));

            assertThatThrownBy(() -> authService.verifyOtp(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("OTP has expired. Please request a new one.");
        }

        @Test
        @DisplayName("Reset password success -> updates password, marks OTP as used")
        void resetPassword_Success() {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setEmail("user@example.com");
            request.setOtp("123456");
            request.setNewPassword("NewPassword123");

            ForgotPassword fp = ForgotPassword.builder()
                    .user(sampleUser)
                    .otpHash("hashed_123456")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .used(false)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
            when(forgotPasswordRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(sampleUser))
                    .thenReturn(Optional.of(fp));
            when(passwordEncoder.matches("123456", "hashed_123456")).thenReturn(true);
            when(passwordEncoder.encode("NewPassword123")).thenReturn("encoded_new_password");
            when(jwtService.generateAccessToken(sampleUser)).thenReturn("sample_access_token");
            when(jwtService.generateRefreshToken(sampleUser)).thenReturn("sample_refresh_token");

            AuthResponse response = authService.resetPassword(request, httpServletResponse);

            assertThat(response).isNotNull();
            assertThat(fp.isUsed()).isTrue();
            verify(userRepository).save(sampleUser);
            verify(forgotPasswordRepository).save(fp);
        }
    }
}
