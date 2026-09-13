package com.gergert.authservice.security.cookie;

import jakarta.servlet.http.HttpServletResponse;
import com.gergert.authservice.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtCookieService {

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final JwtTokenService jwtTokenService;

    public void addAuthenticationCookies(HttpServletResponse response,
                                         String accessToken,
                                         String refreshToken) {
        addCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                accessToken,
                jwtTokenService.getAccessTokenExpirationMs(),
                true
        );

        addCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                jwtTokenService.getRefreshTokenExpirationMs(),
                true
        );
    }

    public void clearAuthenticationCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", 0, true);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", 0, true);
    }

    private void addCookie(HttpServletResponse response,
                           String name,
                           String value,
                           long maxAgeMs,
                           boolean httpOnly) {

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeMs > 0 ? maxAgeMs / 1000 : 0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
