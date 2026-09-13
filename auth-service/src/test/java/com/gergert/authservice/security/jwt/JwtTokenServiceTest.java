package com.gergert.authservice.security.jwt;

import com.gergert.common.dto.jwt.JwtClaimsDto;
import com.gergert.common.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-123456";

    private static final long ACCESS_EXPIRATION_MS = 60_000;
    private static final long REFRESH_EXPIRATION_MS = 120_000;

    private JwtTokenService jwtTokenService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(
                SECRET,
                ACCESS_EXPIRATION_MS,
                REFRESH_EXPIRATION_MS
        );

        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateAccessJwtToken_shouldContainExpectedClaims() {
        var claims = new JwtClaimsDto(42L, Role.ROLE_CUSTOMER);

        String token = jwtTokenService.generateAccessJwtToken(claims);

        var parsed = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(parsed.getSubject()).isEqualTo("42");

        assertThat(parsed.get("role", String.class))
                .isEqualTo(Role.ROLE_CUSTOMER.name());

        assertThat(parsed.get("type", String.class))
                .isEqualTo("ACCESS");

        assertThat(parsed.getId()).isNull();
        assertThat(parsed.getIssuedAt()).isNotNull();
        assertThat(parsed.getExpiration()).isAfter(parsed.getIssuedAt());
    }

    @Test
    void generateRefreshJwtToken_shouldContainExpectedClaims() {
        var claims = new JwtClaimsDto(42L, Role.ROLE_CUSTOMER);

        String token = jwtTokenService.generateRefreshJwtToken(claims);

        var parsed = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(parsed.getSubject()).isEqualTo("42");

        assertThat(parsed.get("role", String.class))
                .isEqualTo(Role.ROLE_CUSTOMER.name());

        assertThat(parsed.get("type", String.class))
                .isEqualTo("REFRESH");

        assertThat(parsed.getId()).isNotNull();
        assertThat(parsed.getId()).isNotBlank();
        assertThat(parsed.getIssuedAt()).isNotNull();
        assertThat(parsed.getExpiration()).isAfter(parsed.getIssuedAt());
    }

    @Test
    void generateRefreshJwtToken_shouldGenerateUniqueTokenIds() {
        var claims = new JwtClaimsDto(42L, Role.ROLE_CUSTOMER);

        String firstToken = jwtTokenService.generateRefreshJwtToken(claims);
        String secondToken = jwtTokenService.generateRefreshJwtToken(claims);

        var firstId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(firstToken)
                .getPayload()
                .getId();

        var secondId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(secondToken)
                .getPayload()
                .getId();

        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    void getRefreshTokenExpirationMs_shouldReturnConfiguredValue() {
        assertThat(jwtTokenService.getRefreshTokenExpirationMs())
                .isEqualTo(REFRESH_EXPIRATION_MS);
    }
}
