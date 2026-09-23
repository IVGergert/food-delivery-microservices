package com.gergert.authservice.kafka;

import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourierUpdatedEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CourierUpdatedEventProducer courierUpdatedEventProducer;

    @Test
    void send_shouldSendCourierUpdatedEvent() {
        CourierUpdatedEventDto event =
                new CourierUpdatedEventDto(
                        10L,
                        "Alex",
                        "Smith"
                );

        ReflectionTestUtils.setField(
                courierUpdatedEventProducer,
                "topic",
                "courier-update-topic"
        );

        courierUpdatedEventProducer.send(event);

        verify(kafkaTemplate).send(
                "courier-update-topic",
                "10",
                event
        );
    }
}