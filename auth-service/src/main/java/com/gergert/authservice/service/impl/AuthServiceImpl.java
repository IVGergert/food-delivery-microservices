package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.*;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.InvalidTokenException;
import com.gergert.authservice.exception.PasswordMismatchException;
import com.gergert.authservice.exception.UserAlreadyExistsException;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.authservice.security.jwt.JwtTokenService;
import com.gergert.authservice.service.AuthService;
import com.gergert.authservice.service.RefreshTokenService;
import com.gergert.common.dto.jwt.JwtClaimsDto;
import com.gergert.common.enums.Role;
import com.gergert.common.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenValidator jwtTokenValidator;


    @Override
    public AuthResultDto login(LoginRequestDto loginDto) {
        log.info("Login attempt for user with email: {}", loginDto.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.email(),
                        loginDto.password()
                )
        );

        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database: {}", loginDto.email());
                    return new UsernameNotFoundException("User with email " + loginDto.email() + " not found");
                });

        log.info("User logged in successfully. User ID: {}, email: {}", user.getId(), user.getEmail());

        return buildAuthResult(user);
    }

    @Override
    @Transactional
    public AuthResultDto register(RegisterRequestDto registerDto) {
        log.info("User registration attempt for email: {}", registerDto.email());

        validateRegistration(registerDto);

        User savedUser = createUser(registerDto);
        log.info("Customer registered successfully. User ID: {}", savedUser.getId());

        return buildAuthResult(savedUser);
    }

    @Override
    public AuthResultDto refresh(String bearerToken) {
        var refreshToken = extractToken(bearerToken);

        if (!jwtTokenValidator.validateJwtToken(refreshToken)) {
            log.warn("Refresh token validation failed");
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (!"REFRESH".equals(jwtTokenValidator.getTokenType(refreshToken))) {
            log.warn("Token type is not REFRESH");
            throw new InvalidTokenException("Invalid refresh token");
        }

        String tokenId = jwtTokenValidator.getTokenId(refreshToken);

        if (!refreshTokenService.exists(tokenId)) {
            log.warn("Refresh token not found in Redis. JTI: {}", tokenId);
            throw new InvalidTokenException("Invalid refresh token");
        }

        JwtClaimsDto claims = jwtTokenValidator.getClaimsFromToken(refreshToken);

        log.info("Refresh token claims extracted. User ID: {}, role: {}",
                claims.userId(),
                claims.role()
        );

        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with userId " + claims.userId() + " not found"
                ));

        refreshTokenService.delete(tokenId);

        return buildAuthResult(user);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenValidator.validateJwtToken(refreshToken)) {
            return;
        }

        if (!"REFRESH".equals(jwtTokenValidator.getTokenType(refreshToken))) {
            return;
        }

        String tokenId = jwtTokenValidator.getTokenId(refreshToken);
        refreshTokenService.delete(tokenId);
        log.info("Refresh token revoked during logout. JTI: {}", tokenId);
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return bearerToken;
    }

    private void validateRegistration(RegisterRequestDto registerDto) {
        if (!registerDto.password().equals(registerDto.confirmPassword())) {
            log.warn("Passwords do not match for email: {}", registerDto.email());
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (userRepository.existsByEmail(registerDto.email())) {
            log.warn("User with email already exists: {}", registerDto.email());
            throw new UserAlreadyExistsException("User with email " + registerDto.email() + " already exists");
        }
    }

    private User createUser(RegisterRequestDto registerDto) {
        User user = User.builder()
                .email(registerDto.email())
                .password(passwordEncoder.encode(registerDto.password()))
                .role(Role.ROLE_CUSTOMER)
                .build();

        return userRepository.save(user);
    }

    private AuthResultDto buildAuthResult(User user) {
        JwtClaimsDto claims = new JwtClaimsDto(
                user.getId(),
                user.getRole()
        );

        String accessToken = jwtTokenService.generateAccessJwtToken(claims);
        String refreshToken = jwtTokenService.generateRefreshJwtToken(claims);

        String refreshTokenId = jwtTokenValidator.getTokenId(refreshToken);

        refreshTokenService.save(
                refreshTokenId,
                claims.userId(),
                Duration.ofMillis(jwtTokenService.getRefreshTokenExpirationMs())
        );

        UserResponseDto response = UserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        AuthTokensDto tokens = new AuthTokensDto(
                accessToken,
                refreshToken,
                "Bearer"
        );

        return new AuthResultDto(response, tokens);
    }
}


