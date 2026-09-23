package com.gergert.common.dto.kafka;

public record CourierUpdatedEventDto(
        Long userId,
        String firstName,
        String lastName) {
}
