package com.gergert.authservice.security.jwt;

import com.gergert.common.dto.jwt.JwtClaimsDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final SecretKey secret;
    private final Long expirationMsForAccessToken;
    private final Long expirationMsForRefreshToken;

    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms-jwt-token}") long expirationMsForAccessToken,
            @Value("${jwt.expiration-ms-refresh-token}") long expirationMsForRefreshToken) {

        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMsForAccessToken = expirationMsForAccessToken;
        this.expirationMsForRefreshToken = expirationMsForRefreshToken;
    }

    public String generateAccessJwtToken(JwtClaimsDto dto) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMsForAccessToken);

        return Jwts.builder()
                .subject(dto.userId().toString())
                .claim("role", dto.role().name())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secret)
                .compact();
    }

    public String generateRefreshJwtToken(JwtClaimsDto dto) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMsForRefreshToken);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(dto.userId().toString())
                .claim("role", dto.role().name())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secret)
                .compact();
    }

    public long getAccessTokenExpirationMs() {
        return expirationMsForAccessToken;
    }

    public long getRefreshTokenExpirationMs() {
        return expirationMsForRefreshToken;
    }

}
