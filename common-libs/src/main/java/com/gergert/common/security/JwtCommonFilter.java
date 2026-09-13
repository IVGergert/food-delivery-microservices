package com.gergert.common.security;

import com.gergert.common.dto.jwt.JwtClaimsDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtCommonFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JwtTokenValidator tokenValidator;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {

            String jwtToken = getTokenFromRequest(request);

            if (jwtToken != null && tokenValidator.validateJwtToken(jwtToken)) {
                authenticateUser(jwtToken);
            }
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String jwtToken) {
        if ("ACCESS".equals(tokenValidator.getTokenType(jwtToken))) {

            JwtClaimsDto claims = tokenValidator.getClaimsFromToken(jwtToken);
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(claims.role().name());

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    claims,
                    null,
                    Collections.singletonList(authority)
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.debug("User authenticated: id={}, role={}",
                    claims.userId(),
                    claims.role());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request){
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
