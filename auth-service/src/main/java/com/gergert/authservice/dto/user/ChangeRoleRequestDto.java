package com.gergert.authservice.dto.user;

import com.gergert.common.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequestDto(
        @NotNull(message = "Role cannot be null")
        Role role
) {}
