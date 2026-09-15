package com.gergert.authservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChangeEmailRequestDto(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Incorrect format email")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String currentPassword
) {}
