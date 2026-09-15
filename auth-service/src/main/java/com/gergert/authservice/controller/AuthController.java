package com.gergert.authservice.controller;

import com.gergert.authservice.dto.auth.AuthResultDto;
import com.gergert.authservice.dto.auth.LoginRequestDto;
import com.gergert.authservice.dto.auth.RegisterRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.security.cookie.JwtCookieService;
import com.gergert.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @Operation(security = {})
    @GetMapping("/csrf")
    public ResponseEntity<String> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(csrfToken.getToken());
    }

    @Operation(security = {})
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto,
                                                 HttpServletResponse response) {

        AuthResultDto authResultDto = authService.login(loginDto);

        jwtCookieService.addAuthenticationCookies(
                response,
                authResultDto.tokens().accessToken(),
                authResultDto.tokens().refreshToken()
        );

        return ResponseEntity.ok(authResultDto.response());
    }

    @Operation(security = {})
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerDto,
                                                    HttpServletResponse response) {

        AuthResultDto authResultDto = authService.register(registerDto);

        jwtCookieService.addAuthenticationCookies(
                response,
                authResultDto.tokens().accessToken(),
                authResultDto.tokens().refreshToken()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(authResultDto.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponseDto> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                                   HttpServletResponse response) {

        AuthResultDto authResultDto = authService.refresh(refreshToken);

        jwtCookieService.addAuthenticationCookies(
                response,
                authResultDto.tokens().accessToken(),
                authResultDto.tokens().refreshToken()
        );

        return ResponseEntity.ok(authResultDto.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                       HttpServletResponse response) {

        authService.logout(refreshToken);
        jwtCookieService.clearAuthenticationCookies(response);
        return ResponseEntity.noContent().build();
    }
}
