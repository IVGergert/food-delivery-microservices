package com.gergert.authservice.dto.auth;

import com.gergert.authservice.dto.user.UserResponseDto;

public record AuthResultDto (
        UserResponseDto response,
        AuthTokensDto tokens)
{}
