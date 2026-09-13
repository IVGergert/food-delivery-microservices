package com.gergert.authservice.dto;

import lombok.Builder;

@Builder
public record AuthTokensDto(
        String accessToken,
        String refreshToken,
        String tokenType)
{}
