package com.gergert.authservice.dto.user;

import com.gergert.common.enums.Role;
import lombok.Builder;

@Builder
public record UserResponseDto(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role)
{}
