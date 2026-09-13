package com.gergert.authservice.controller;

import com.gergert.authservice.dto.*;
import com.gergert.authservice.security.cookie.JwtCookieService;
import com.gergert.authservice.service.AuthService;
import com.gergert.common.enums.Role;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.web.csrf.CsrfToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    AuthService authService;

    @Mock
    JwtCookieService jwtCookieService;

    @Mock
    HttpServletResponse response;

    @Mock
    CsrfToken csrfToken;

    private AuthController controller;

    @BeforeEach
    void setUp() { controller = new AuthController(authService, jwtCookieService); }

    @Test
    void csrf_shouldReturnToken() {
        when(csrfToken.getToken()).thenReturn("csrf-token");
        var result = controller.csrf(csrfToken);
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo("csrf-token");
    }

    @Test
    void login_shouldSetAuthenticationCookiesAndReturnUser() {
        var request = new LoginRequestDto("user@example.com", "password");
        var user = new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER);
        var result = new AuthResultDto(user, new AuthTokensDto("access", "refresh", "Bearer"));
        when(authService.login(request)).thenReturn(result);

        var responseEntity = controller.login(request, response);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo(user);
        verify(jwtCookieService).addAuthenticationCookies(response, "access", "refresh");
    }

    @Test
    void register_shouldSetAuthenticationCookiesAndReturnCreated() {
        var request = new RegisterRequestDto("user@example.com", "password", "password");
        var user = new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER);
        var result = new AuthResultDto(user, new AuthTokensDto("access", "refresh", "Bearer"));
        when(authService.register(request)).thenReturn(result);

        var responseEntity = controller.register(request, response);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(201);
        assertThat(responseEntity.getBody()).isEqualTo(user);
        verify(jwtCookieService).addAuthenticationCookies(response, "access", "refresh");
    }

    @Test
    void refresh_shouldReadRefreshCookieAndRotateAuthenticationCookies() {
        var user = new UserResponseDto(1L, "user@example.com", Role.ROLE_CUSTOMER);
        var result = new AuthResultDto(user, new AuthTokensDto("new-access", "new-refresh", "Bearer"));
        when(authService.refresh("old-refresh")).thenReturn(result);

        var responseEntity = controller.refresh("old-refresh", response);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo(user);
        verify(authService).refresh("old-refresh");
        verify(jwtCookieService).addAuthenticationCookies(response, "new-access", "new-refresh");
    }

    @Test
    void logout_shouldRevokeRefreshCookieAndClearAuthenticationCookies() {
        var responseEntity = controller.logout("refresh-token", response);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
        verify(authService).logout("refresh-token");
        verify(jwtCookieService).clearAuthenticationCookies(response);
    }


}
