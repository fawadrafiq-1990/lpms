package com.company.legal.auth.service;

import com.company.legal.auth.dto.*;
import com.company.legal.auth.service.JwtService;
import com.company.legal.common.exception.BaseException;
import com.company.legal.user.entity.Role;
import com.company.legal.user.entity.User;
import com.company.legal.user.repository.RoleRepository;
import com.company.legal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id("user-1")
                .email("test@example.com")
                .passwordHash("$2a$12$hash")
                .firstName("Test")
                .lastName("User")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        Role role = Role.builder().id("role-1").name("LAWYER").build();
        activeUser.getRoles().add(role);

        mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(activeUser);
    }

    // ========== LOGIN TESTS ==========

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_success() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(activeUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(jwtService.generateToken(mockAuthentication)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
            assertThat(response.getUser().getFullName()).isEqualTo("Test User");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
            assertThat(savedUser.getLastLoginAt()).isNotNull();
            verify(userRepository).save(any());
            verify(jwtService).generateToken(mockAuthentication);
            verify(jwtService).generateRefreshToken(activeUser);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid email")
        void login_invalidEmail() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("nonexistent@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid email or password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for wrong password")
        void login_wrongPassword() {
            // Arrange
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(activeUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should lock account after 5 failed attempts")
        void login_accountLockoutAfterFiveAttempts() {
            // Arrange
            User lockedUser = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordHash("$2a$12$hash")
                    .firstName("Test")
                    .lastName("User")
                    .status(User.UserStatus.ACTIVE)
                    .failedLoginAttempts(4)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(lockedUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(savedUser.getLockedUntil()).isNotNull();
            assertThat(savedUser.getLockedUntil()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should throw LockedException for locked account")
        void login_lockedAccount() {
            // Arrange
            User lockedUser = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordHash("$2a$12$hash")
                    .firstName("Test")
                    .lastName("User")
                    .status(User.UserStatus.ACTIVE)
                    .lockedUntil(LocalDateTime.now().plusMinutes(30))
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(lockedUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("Account is locked");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("Should reject inactive account")
        void login_inactiveAccount() {
            // Arrange
            User inactiveUser = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordHash("$2a$12$hash")
                    .firstName("Inactive")
                    .lastName("User")
                    .status(User.UserStatus.INACTIVE)
                    .build();

            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Account is not active");

            verify(authenticationManager, never()).authenticate(any());
        }
    }

    // ========== REGISTER TESTS ==========

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new lawyer user successfully")
        void register_lawyer_success() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .email("newlawyer@example.com")
                    .password("SecurePass1")
                    .firstName("John")
                    .lastName("Doe")
                    .isClient(false)
                    .build();

            when(userRepository.existsByEmail("newlawyer@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SecurePass1")).thenReturn("encodedHash");
            when(roleRepository.findByName("LAWYER")).thenReturn(Optional.of(
                    Role.builder().id("role-1").name("LAWYER").build()
            ));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("new-user-id");
                return user;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getUser().getEmail()).isEqualTo("newlawyer@example.com");
            assertThat(response.getUser().getFirstName()).isEqualTo("John");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getStatus()).isEqualTo(User.UserStatus.PENDING);
            assertThat(savedUser.getRoles()).hasSize(1);
            assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("LAWYER");
            assertThat(savedUser.getEmailVerificationToken()).isNotNull();
            assertThat(savedUser.getEmailVerificationTokenExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should register a new client user successfully")
        void register_client_success() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .email("newclient@example.com")
                    .password("SecurePass1")
                    .firstName("Jane")
                    .lastName("Smith")
                    .isClient(true)
                    .build();

            when(userRepository.existsByEmail("newclient@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SecurePass1")).thenReturn("encodedHash");
            when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(
                    Role.builder().id("role-2").name("CLIENT").build()
            ));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("new-user-id");
                return user;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response).isNotNull();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRoles())
                    .anyMatch(r -> r.getName().equals("CLIENT"));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_emailAlreadyExists() {
            // Arrange
            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@example.com")
                    .password("SecurePass1")
                    .firstName("Existing")
                    .lastName("User")
                    .build();

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "EMAIL_EXISTS");

            verify(userRepository, never()).save(any());
        }
    }

    // ========== REFRESH TOKEN TESTS ==========

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_success() {
            // Arrange
            User user = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .status(User.UserStatus.ACTIVE)
                    .build();

            when(jwtService.isTokenValid("valid-refresh-token")).thenReturn(true);
            when(jwtService.extractEmail("valid-refresh-token")).thenReturn("test@example.com");
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(User.class))).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("new-refresh-token");

            // Act
            AuthResponse response = authService.refreshToken("valid-refresh-token");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("Should throw exception for invalid refresh token")
        void refreshToken_invalidToken() {
            // Arrange
            when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TOKEN");

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void refreshToken_userNotFound() {
            // Arrange
            when(jwtService.isTokenValid("valid-token")).thenReturn(true);
            when(jwtService.extractEmail("valid-token")).thenReturn("missing@example.com");
            when(userRepository.findByEmail("missing@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken("valid-token"))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "USER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should throw exception for inactive user")
        void refreshToken_inactiveUser() {
            // Arrange
            User inactiveUser = User.builder()
                    .id("user-1")
                    .email("inactive@example.com")
                    .status(User.UserStatus.INACTIVE)
                    .build();

            when(jwtService.isTokenValid("valid-token")).thenReturn(true);
            when(jwtService.extractEmail("valid-token")).thenReturn("inactive@example.com");
            when(userRepository.findByEmail("inactive@example.com"))
                    .thenReturn(Optional.of(inactiveUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken("valid-token"))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ACCOUNT_NOT_ACTIVE");
        }
    }

    // ========== PASSWORD RESET TESTS ==========

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should generate reset token for existing user")
        void forgotPassword_existingUser() {
            // Arrange
            User user = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .status(User.UserStatus.ACTIVE)
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));

            // Act
            authService.forgotPassword("test@example.com");

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordResetToken()).isNotNull();
            assertThat(savedUser.getPasswordResetTokenExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should do nothing for non-existent user (security)")
        void forgotPassword_nonExistentUser() {
            // Arrange
            when(userRepository.findByEmail("missing@example.com"))
                    .thenReturn(Optional.empty());

            // Act
            authService.forgotPassword("missing@example.com");

            // Assert
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void resetPassword_success() {
            // Arrange
            User user = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordHash("$2a$12$oldHash")
                    .passwordResetToken("valid-reset-token")
                    .passwordResetTokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .status(User.UserStatus.ACTIVE)
                    .build();

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .email("test@example.com")
                    .token("valid-reset-token")
                    .newPassword("NewSecurePass1")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewSecurePass1")).thenReturn("$2a$12$newHash");

            // Act
            authService.resetPassword(request);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$12$newHash");
            assertThat(savedUser.getPasswordResetToken()).isNull();
            assertThat(savedUser.getPasswordResetTokenExpiresAt()).isNull();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(0);
            assertThat(savedUser.getLockedUntil()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when user not found during password reset")
        void resetPassword_userNotFound() {
            // Arrange
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .email("missing@example.com")
                    .token("some-token")
                    .newPassword("NewSecurePass1")
                    .build();

            when(userRepository.findByEmail("missing@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "USER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should throw exception for invalid reset token")
        void resetPassword_invalidToken() {
            // Arrange
            User user = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordResetToken("different-token")
                    .build();

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .email("test@example.com")
                    .token("wrong-token")
                    .newPassword("NewSecurePass1")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TOKEN");
        }

        @Test
        @DisplayName("Should throw exception for expired reset token")
        void resetPassword_expiredToken() {
            // Arrange
            User user = User.builder()
                    .id("user-1")
                    .email("test@example.com")
                    .passwordResetToken("valid-token")
                    .passwordResetTokenExpiresAt(LocalDateTime.now().minusHours(1))
                    .build();

            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .email("test@example.com")
                    .token("valid-token")
                    .newPassword("NewSecurePass1")
                    .build();

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BaseException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TOKEN_EXPIRED");
        }
    }

    // ========== LOGOUT TESTS ==========

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should handle logout gracefully")
        void logout_success() {
            // Act - logout is currently a no-op in stateless JWT setup
            authService.logout("user-1");

            // Assert - no exceptions thrown, no repository calls made
            verify(userRepository, never()).save(any());
        }
    }
}
