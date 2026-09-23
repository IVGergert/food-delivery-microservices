package com.gergert.authservice.kafka;


import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourierUpdatedEventProducer {
    @Value("${kafka.topics.courier-update-events}")
    private String topic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(CourierUpdatedEventDto event) {
        kafkaTemplate.send(
                topic,
                String.valueOf(event.userId()),
                event
        );
    }
}
