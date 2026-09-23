package com.gergert.authservice.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequestDto(
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, max = 16, message = "Password must be between 6 and 16 characters long.")
        String newPassword,

        @NotBlank(message = "Confirm password cannot be empty")
        @Size(min = 6, max = 16, message = "Confirm password must be between 6 and 16 characters long.")
        String confirmPassword
) {}
