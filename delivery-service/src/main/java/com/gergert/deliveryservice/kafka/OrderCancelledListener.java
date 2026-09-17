package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.OrderCancelledEventDto;
import com.gergert.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledListener {
    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${kafka.topics.order-cancelled-events}",
            groupId = "delivery-group"
    )

    public void handle(OrderCancelledEventDto eventDto) {
        log.info("Received OrderCancelledEvent: orderId={}", eventDto.orderId());
        deliveryService.cancelDelivery(eventDto);
    }
}
