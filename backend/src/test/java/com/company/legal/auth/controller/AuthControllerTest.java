package com.company.legal.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.legal.auth.dto.*;
import com.company.legal.auth.service.AuthService;
import com.company.legal.common.exception.GlobalExceptionHandler;
import com.company.legal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        UserInfoDto userInfo = UserInfoDto.builder()
                .id("user-1")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .status("ACTIVE")
                .roles(Set.of("LAWYER"))
                .lawFirmId("firm-1")
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("jwt-access-token-here")
                .refreshToken("jwt-refresh-token-here")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userInfo)
                .build();
    }

    @Test
    @DisplayName("Login - Should return auth response on valid credentials")
    void login_success() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass1")
                .build();

        when(authService.login(request)).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token-here"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.user.roles").isArray());
    }

    @Test
    @DisplayName("Login - Should return 400 for missing email")
    void login_missingEmail() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .password("SomePassword1")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Login - Should return 400 for invalid email format")
    void login_invalidEmailFormat() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("not-an-email")
                .password("SecurePass1")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Should create user successfully")
    void register_success() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("SecurePass1")
                .firstName("New")
                .lastName("User")
                .build();

        AuthResponse regResponse = AuthResponse.builder()
                .accessToken("reg-access-token")
                .refreshToken("reg-refresh-token")
                .tokenType("Bearer")
                .user(UserInfoDto.builder()
                        .id("new-user")
                        .email("new@example.com")
                        .firstName("New")
                        .lastName("User")
                        .status("PENDING")
                        .roles(Set.of("LAWYER"))
                        .build())
                .build();

        when(authService.register(request)).thenReturn(regResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("reg-access-token"))
                .andExpect(jsonPath("$.data.user.status").value("PENDING"));
    }

    @Test
    @DisplayName("Register - Should return 400 for weak password")
    void register_weakPassword() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("weak")
                .firstName("New")
                .lastName("User")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - Should return 400 for missing first name")
    void register_missingFirstName() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("SecurePass1")
                .lastName("User")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Refresh Token - Should return new tokens")
    void refreshToken_success() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        AuthResponse newResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .user(UserInfoDto.builder()
                        .id("user-1")
                        .email("test@example.com")
                        .build())
                .build();

        when(authService.refreshToken("old-refresh-token")).thenReturn(newResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("Refresh Token - Should return 400 for missing token")
    void refreshToken_missingToken() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("");

        // Act & Assert
        mockMvc.perform(post("/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Forgot Password - Should accept valid email")
    void forgotPassword_success() throws Exception {
        // Arrange
        when(authService.forgotPassword("test@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Reset Password - Should reset password successfully")
    void resetPassword_success() throws Exception {
        // Arrange
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("test@example.com")
                .token("reset-token-123")
                .newPassword("NewSecurePass1")
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        verify(authService).resetPassword(request);
    }

    @Test
    @DisplayName("Reset Password - Should return 400 for missing fields")
    void resetPassword_missingFields() throws Exception {
        // Arrange
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("test@example.com")
                .token("token")
                .build(); // newPassword missing

        // Act & Assert
        mockMvc.perform(post("/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Logout - Should call service method")
    void logout_success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/auth/logout")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout("user-1");
    }

    @Test
    @DisplayName("Login - Should return 401 for bad credentials")
    void login_badCredentials() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(authService.login(request))
                .thenThrow(org.springframework.security.authentication.BadCredentialsException.class);

        // Act & Assert
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Register - Should return 400 when email exists")
    void register_emailExists() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("SecurePass1")
                .firstName("Existing")
                .lastName("User")
                .build();

        when(authService.register(request))
                .thenThrow(new com.company.legal.common.exception.BaseException("EMAIL_EXISTS", "Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_EXISTS"));
    }
}
