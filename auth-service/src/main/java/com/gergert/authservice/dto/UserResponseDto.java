package com.gergert.authservice.dto;

import com.gergert.common.enums.Role;
import lombok.Builder;

@Builder
public record UserResponseDto(
        Long userId,
        String email,
        Role role)
{}
