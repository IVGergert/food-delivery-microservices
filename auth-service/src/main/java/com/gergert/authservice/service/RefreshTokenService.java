package com.gergert.authservice.service;

import java.time.Duration;

public interface RefreshTokenService {
    void save(String tokenId, Long userId, Duration expiration);
    boolean exists(String tokenId);
    void delete(String tokenId);
}
