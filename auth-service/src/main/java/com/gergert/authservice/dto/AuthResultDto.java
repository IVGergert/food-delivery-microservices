package com.gergert.authservice.dto;

public record AuthResultDto (
        UserResponseDto response,
        AuthTokensDto tokens)
{}
