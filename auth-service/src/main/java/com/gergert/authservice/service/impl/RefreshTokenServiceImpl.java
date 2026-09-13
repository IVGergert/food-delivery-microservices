package com.gergert.authservice.service.impl;

import com.gergert.authservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String tokenId, Long userId, Duration expiration) {
        String key = REFRESH_TOKEN_PREFIX + tokenId;

        redisTemplate.opsForValue().set(
                key,
                userId.toString(),
                expiration
        );
    }

    @Override
    public boolean exists(String tokenId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + tokenId)
        );
    }

    @Override
    public void delete(String tokenId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId);
    }
}
