package com.gergert.deliveryservice.kafka;

import com.gergert.common.dto.kafka.CourierCreatedEventDto;
import com.gergert.deliveryservice.entity.Courier;
import com.gergert.deliveryservice.entity.CourierStatus;
import com.gergert.deliveryservice.entity.TransportType;
import com.gergert.deliveryservice.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierCreatedListenerTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierCreatedListener listener;

    @Test
    void handle_shouldCreateOfflineCourier() {
        CourierCreatedEventDto event = new CourierCreatedEventDto(
                10L,
                "alex@example.com",
                "Alex",
                "Smith",
                "CAR"
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        listener.handle(event);

        ArgumentCaptor<Courier> captor =
                ArgumentCaptor.forClass(Courier.class);

        verify(courierRepository)
                .save(captor.capture());

        Courier saved = captor.getValue();

        assertThat(saved.getUserId())
                .isEqualTo(10L);

        assertThat(saved.getFirstName())
                .isEqualTo("Alex");

        assertThat(saved.getLastName())
                .isEqualTo("Smith");

        assertThat(saved.getTransportType())
                .isEqualTo(TransportType.CAR);

        assertThat(saved.getCourierStatus())
                .isEqualTo(CourierStatus.OFFLINE);
    }

    @Test
    void handle_shouldIgnoreExistingCourier() {
        CourierCreatedEventDto event = new CourierCreatedEventDto(
                10L,
                "alex@example.com",
                "Alex",
                "Smith",
                "CAR"
        );

        when(courierRepository.findByUserId(10L)).thenReturn(
                        Optional.of(
                                Courier.builder()
                                        .id(1L)
                                        .userId(10L)
                                        .build()
                        )
                );

        listener.handle(event);

        verify(courierRepository, never()).save(any(Courier.class));
    }
}