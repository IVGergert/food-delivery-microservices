package com.gergert.authservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminUpdateUserRequestDto(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Incorrect format email")
        String email,

        @NotBlank(message = "First name cannot be empty")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        String lastName
) {}
