package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.deliveryservice.entity.Courier;
import com.gergert.deliveryservice.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourierUpdatedListener {
    private final CourierRepository courierRepository;

    @KafkaListener(
            topics = "${kafka.topics.courier-update-events}",
            groupId = "delivery-service"
    )

    public void handle(CourierUpdatedEventDto event) {
        log.info("Received CourierUpdatedEvent for userId={}", event.userId());

        Courier courier = courierRepository
                .findByUserId(event.userId())
                .orElse(null);

        if (courier == null) {
            log.warn("Courier not found for userId={}", event.userId());
            return;
        }

        courier.setFirstName(event.firstName());
        courier.setLastName(event.lastName());

        courierRepository.save(courier);

        log.info("Courier {} name updated successfully", event.userId());
    }
}
