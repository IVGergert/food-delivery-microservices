package com.gergert.authservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {
    private static final String TOKEN_ID = "test-jti";
    private static final Long USER_ID = 42L;
    private static final Duration EXPIRATION = Duration.ofDays(14);

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(redisTemplate);
    }

    @Test
    void save_shouldStoreTokenWithExpiration() {
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        refreshTokenService.save(
                TOKEN_ID,
                USER_ID,
                EXPIRATION
        );

        verify(valueOperations).set(
                "refresh:" + TOKEN_ID,
                USER_ID.toString(),
                EXPIRATION
        );
    }

    @Test
    void exists_shouldReturnTrueWhenTokenExists() {
        when(redisTemplate.hasKey("refresh:" + TOKEN_ID))
                .thenReturn(true);

        assertThat(
                refreshTokenService.exists(TOKEN_ID)
        ).isTrue();

        verify(redisTemplate)
                .hasKey("refresh:" + TOKEN_ID);
    }

    @Test
    void exists_shouldReturnFalseWhenTokenDoesNotExist() {
        when(redisTemplate.hasKey("refresh:" + TOKEN_ID))
                .thenReturn(false);

        assertThat(
                refreshTokenService.exists(TOKEN_ID)
        ).isFalse();
    }

    @Test
    void exists_shouldReturnFalseWhenRedisReturnsNull() {
        when(redisTemplate.hasKey("refresh:" + TOKEN_ID))
                .thenReturn(null);

        assertThat(
                refreshTokenService.exists(TOKEN_ID)
        ).isFalse();
    }

    @Test
    void delete_shouldDeleteToken() {
        refreshTokenService.delete(TOKEN_ID);

        verify(redisTemplate)
                .delete("refresh:" + TOKEN_ID);
    }
}

