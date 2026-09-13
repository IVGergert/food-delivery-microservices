package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.AuthResultDto;
import com.gergert.authservice.dto.AuthTokensDto;
import com.gergert.authservice.dto.LoginRequestDto;
import com.gergert.authservice.dto.RegisterRequestDto;
import com.gergert.authservice.dto.UserResponseDto;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.InvalidTokenException;
import com.gergert.authservice.exception.PasswordMismatchException;
import com.gergert.authservice.exception.UserAlreadyExistsException;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.authservice.security.jwt.JwtTokenService;
import com.gergert.authservice.service.RefreshTokenService;
import com.gergert.common.dto.jwt.JwtClaimsDto;
import com.gergert.common.enums.Role;
import com.gergert.common.security.JwtTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final long REFRESH_EXPIRATION_MS = 120_000;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .role(Role.ROLE_CUSTOMER)
                .build();
    }

    @Test
    void register_shouldCreateCustomerAndReturnAuthResult() {
        var request = new RegisterRequestDto("user@example.com", "password", "password");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        stubTokens();

        AuthResultDto result = authService.register(request);

        assertThat(result.response()).isEqualTo(new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER));
        assertThat(result.tokens()).isEqualTo(new AuthTokensDto("access-token", "refresh-token", "Bearer"));
        verify(authenticationManager, never()).authenticate(any());
        verify(refreshTokenService).save(eq("refresh-jti"), eq(1L), eq(Duration.ofMillis(REFRESH_EXPIRATION_MS)));
    }

    @Test
    void register_shouldRejectWhenPasswordsDoNotMatch() {
        var request = new RegisterRequestDto("user@example.com", "password", "different");
        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(PasswordMismatchException.class);
        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenService, refreshTokenService);
    }

    @Test
    void register_shouldRejectExistingEmail() {
        var request = new RegisterRequestDto("user@example.com", "password", "password");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);
        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldAuthenticateAndReturnAuthResult() {
        var request = new LoginRequestDto("user@example.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        stubTokens();

        AuthResultDto result = authService.login(request);

        assertThat(result.response()).isEqualTo(new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER));
        assertThat(result.tokens()).isEqualTo(new AuthTokensDto("access-token", "refresh-token", "Bearer"));
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(request.email());
        verify(refreshTokenService).save(eq("refresh-jti"), eq(1L), eq(Duration.ofMillis(REFRESH_EXPIRATION_MS)));
    }

    @Test
    void login_shouldPropagateAuthenticationFailure() {
        var request = new LoginRequestDto("user@example.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);
        verify(userRepository, never()).findByEmail(anyString());
        verifyNoInteractions(jwtTokenService, refreshTokenService);
    }

    @Test
    void login_shouldThrowWhenAuthenticatedUserIsMissing() {
        var request = new LoginRequestDto("user@example.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(UsernameNotFoundException.class);
        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtTokenService, refreshTokenService);
    }

    @Test
    void refresh_shouldRotateRefreshToken() {
        String oldToken = "old-refresh-token";
        var claims = new JwtClaimsDto(user.getId(), user.getRole());
        when(jwtTokenValidator.validateJwtToken(oldToken)).thenReturn(true);
        when(jwtTokenValidator.getTokenType(oldToken)).thenReturn("REFRESH");
        when(jwtTokenValidator.getTokenId(oldToken)).thenReturn("old-jti");
        when(refreshTokenService.exists("old-jti")).thenReturn(true);
        when(jwtTokenValidator.getClaimsFromToken(oldToken)).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessJwtToken(claims)).thenReturn("new-access-token");
        when(jwtTokenService.generateRefreshJwtToken(claims)).thenReturn("new-refresh-token");
        when(jwtTokenValidator.getTokenId("new-refresh-token")).thenReturn("new-jti");
        when(jwtTokenService.getRefreshTokenExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);

        AuthResultDto result = authService.refresh(oldToken);

        assertThat(result.response()).isEqualTo(new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER));
        assertThat(result.tokens()).isEqualTo(new AuthTokensDto("new-access-token", "new-refresh-token", "Bearer"));
        verify(refreshTokenService).delete("old-jti");
        verify(refreshTokenService).save(eq("new-jti"), eq(1L), eq(Duration.ofMillis(REFRESH_EXPIRATION_MS)));
    }

    @Test
    void refresh_shouldRejectInvalidJwt() {
        when(jwtTokenValidator.validateJwtToken("invalid-token")).thenReturn(false);
        assertThatThrownBy(() -> authService.refresh("invalid-token"))
                .isInstanceOf(InvalidTokenException.class).hasMessage("Invalid refresh token");
        verifyNoInteractions(refreshTokenService, jwtTokenService);
    }

    @Test
    void refresh_shouldRejectAccessToken() {
        when(jwtTokenValidator.validateJwtToken("access-token")).thenReturn(true);
        when(jwtTokenValidator.getTokenType("access-token")).thenReturn("ACCESS");
        assertThatThrownBy(() -> authService.refresh("access-token"))
                .isInstanceOf(InvalidTokenException.class).hasMessage("Invalid refresh token");
        verifyNoInteractions(refreshTokenService, jwtTokenService);
    }

    @Test
    void refresh_shouldRejectTokenNotFoundInRedis() {
        when(jwtTokenValidator.validateJwtToken("refresh-token")).thenReturn(true);
        when(jwtTokenValidator.getTokenType("refresh-token")).thenReturn("REFRESH");
        when(jwtTokenValidator.getTokenId("refresh-token")).thenReturn("refresh-jti");
        when(refreshTokenService.exists("refresh-jti")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("refresh-token"))
                .isInstanceOf(InvalidTokenException.class).hasMessage("Invalid refresh token");
        verify(refreshTokenService).exists("refresh-jti");
        verify(refreshTokenService, never()).delete(anyString());
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void refresh_shouldRejectNullToken() {
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(InvalidTokenException.class).hasMessage("Invalid refresh token");
        verifyNoInteractions(jwtTokenValidator, refreshTokenService, jwtTokenService);
    }

    @Test
    void logout_shouldDeleteValidRefreshToken() {
        when(jwtTokenValidator.validateJwtToken("refresh-token")).thenReturn(true);
        when(jwtTokenValidator.getTokenType("refresh-token")).thenReturn("REFRESH");
        when(jwtTokenValidator.getTokenId("refresh-token")).thenReturn("refresh-jti");

        authService.logout("refresh-token");

        verify(refreshTokenService).delete("refresh-jti");
    }

    @Test
    void logout_shouldDoNothingForNullToken() {
        authService.logout(null);
        verifyNoInteractions(jwtTokenValidator, refreshTokenService);
    }

    @Test
    void logout_shouldDoNothingForInvalidToken() {
        when(jwtTokenValidator.validateJwtToken("invalid")).thenReturn(false);
        authService.logout("invalid");
        verify(refreshTokenService, never()).delete(anyString());
    }

    @Test
    void logout_shouldDoNothingForAccessToken() {
        when(jwtTokenValidator.validateJwtToken("access")).thenReturn(true);
        when(jwtTokenValidator.getTokenType("access")).thenReturn("ACCESS");
        authService.logout("access");
        verify(refreshTokenService, never()).delete(anyString());
    }

    private void stubTokens() {
        when(jwtTokenService.generateAccessJwtToken(any(JwtClaimsDto.class))).thenReturn("access-token");
        when(jwtTokenService.generateRefreshJwtToken(any(JwtClaimsDto.class))).thenReturn("refresh-token");
        when(jwtTokenValidator.getTokenId("refresh-token")).thenReturn("refresh-jti");
        when(jwtTokenService.getRefreshTokenExpirationMs()).thenReturn(REFRESH_EXPIRATION_MS);
    }
}
