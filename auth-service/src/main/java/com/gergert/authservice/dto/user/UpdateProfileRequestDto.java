package com.gergert.authservice.dto.user;

public record UpdateProfileRequestDto(
        String firstName,
        String lastName
) {}
