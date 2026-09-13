package com.gergert.authservice.security.cookie;

import com.gergert.authservice.security.jwt.JwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtCookieServiceTest {
    private static final long ACCESS_MS = 60_000;
    private static final long REFRESH_MS = 120_000;

    private JwtTokenService jwtTokenService;
    private JwtCookieService cookieService;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtTokenService = mock(JwtTokenService.class);
        cookieService = new JwtCookieService(jwtTokenService);
        response = mock(HttpServletResponse.class);
        when(jwtTokenService.getAccessTokenExpirationMs()).thenReturn(ACCESS_MS);
        when(jwtTokenService.getRefreshTokenExpirationMs()).thenReturn(REFRESH_MS);
    }

    @Test
    void addAuthenticationCookies_shouldSetHttpOnlySecureCookies() {
        cookieService.addAuthenticationCookies(response, "access-token", "refresh-token");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), captor.capture());

        assertThat(captor.getAllValues()).anyMatch(v ->
                v.contains("accessToken=access-token")
                && v.contains("Max-Age=60")
                && v.contains("HttpOnly")
                && v.contains("Secure")
                && v.contains("Path=/")
                && v.contains("SameSite=Lax"));

        assertThat(captor.getAllValues()).anyMatch(v -> v.contains("refreshToken=refresh-token")
                && v.contains("Max-Age=120")
                && v.contains("HttpOnly")
                && v.contains("Secure"));
    }

    @Test
    void clearAuthenticationCookies_shouldExpireBothCookies() {
        cookieService.clearAuthenticationCookies(response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), captor.capture());
        assertThat(captor.getAllValues()).allMatch(v -> v.contains("Max-Age=0") && v.contains("HttpOnly") && v.contains("Secure"));
    }

}
