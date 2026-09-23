package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.deliveryservice.entity.Courier;
import com.gergert.deliveryservice.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierUpdatedListenerTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierUpdatedListener courierUpdatedListener;

    @Test
    void handle_shouldUpdateCourierName() {
        Courier courier = Courier.builder()
                .id(1L)
                .userId(10L)
                .firstName("Ivan")
                .lastName("Petrov")
                .build();

        CourierUpdatedEventDto event = new CourierUpdatedEventDto(
                10L,
                "Alex",
                "Smith"
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        courierUpdatedListener.handle(event);

        assertThat(courier.getFirstName())
                .isEqualTo("Alex");

        assertThat(courier.getLastName())
                .isEqualTo("Smith");

        verify(courierRepository)
                .save(courier);
    }

    @Test
    void handle_shouldDoNothing_whenCourierNotFound() {
        CourierUpdatedEventDto event = new CourierUpdatedEventDto(
                10L,
                "Alex",
                "Smith"
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        courierUpdatedListener.handle(event);

        verify(courierRepository)
                .findByUserId(10L);

        verify(courierRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any());
    }
}