package com.gergert.deliveryservice.service.impl;

import com.gergert.deliveryservice.entity.Courier;
import com.gergert.deliveryservice.entity.CourierStatus;
import com.gergert.deliveryservice.exception.CourierNotAvailableException;
import com.gergert.deliveryservice.exception.CourierNotFoundException;
import com.gergert.deliveryservice.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceImplTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierServiceImpl service;

    @Test
    void goOnline_shouldChangeOfflineToAvailableAndSave() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.OFFLINE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        when(courierRepository.save(courier))
                .thenReturn(courier);

        var response = service.goOnline(10L);

        assertThat(response.status())
                .isEqualTo(CourierStatus.AVAILABLE);

        assertThat(courier.getCourierStatus())
                .isEqualTo(CourierStatus.AVAILABLE);

        verify(courierRepository)
                .save(courier);
    }

    @Test
    void goOnline_shouldNotSaveAlreadyAvailableCourier() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.AVAILABLE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        var response = service.goOnline(10L);

        assertThat(response.status())
                .isEqualTo(CourierStatus.AVAILABLE);

        verify(courierRepository, never())
                .save(any(Courier.class));
    }

    @Test
    void goOnline_shouldThrowWhenCourierDoesNotExist() {
        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.goOnline(10L))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessage("Courier not found for userId = 10");
    }

    @Test
    void goOffline_shouldChangeAvailableToOffline() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.AVAILABLE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        when(courierRepository.save(courier))
                .thenReturn(courier);

        var response = service.goOffline(10L);

        assertThat(response.status())
                .isEqualTo(CourierStatus.OFFLINE);

        assertThat(courier.getCourierStatus())
                .isEqualTo(CourierStatus.OFFLINE);

        verify(courierRepository)
                .save(courier);
    }

    @Test
    void goOffline_shouldRejectCourierWithActiveDelivery() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.ON_THE_WAY_TO_CUSTOMER
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> service.goOffline(10L))
                .isInstanceOf(CourierNotAvailableException.class)
                .hasMessage(
                        "Cannot go offline while having an active delivery!"
                );

        verify(courierRepository, never())
                .save(any(Courier.class));
    }

    @Test
    void goOffline_shouldThrowWhenCourierDoesNotExist() {
        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.goOffline(10L))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessage("Courier not found for userId = 10");
    }

    @Test
    void getStatus_shouldReturnCurrentStatus() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.AVAILABLE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        var response = service.getStatus(10L);

        assertThat(response.status())
                .isEqualTo(CourierStatus.AVAILABLE);
    }

    @Test
    void getStatus_shouldThrowWhenCourierDoesNotExist() {
        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatus(10L))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessage("Courier not found for userId = 10");
    }

    @Test
    void validateLogout_shouldAllowOfflineCourier() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.OFFLINE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        service.validateLogout(10L);

        verify(courierRepository)
                .findByUserId(10L);
    }

    @Test
    void validateLogout_shouldRejectOnlineCourier() {
        Courier courier = courier(
                1L,
                10L,
                CourierStatus.AVAILABLE
        );

        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> service.validateLogout(10L))
                .isInstanceOf(CourierNotAvailableException.class)
                .hasMessage(
                        "Cannot logout while courier is online or has an active delivery!"
                );
    }

    @Test
    void validateLogout_shouldThrowWhenCourierDoesNotExist() {
        when(courierRepository.findByUserId(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateLogout(10L))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessage("Courier not found for userId = 10");
    }

    private Courier courier(Long id,
                            Long userId,
                            CourierStatus status) {

        return Courier.builder()
                .id(id)
                .userId(userId)
                .firstName("Alex")
                .lastName("Smith")
                .courierStatus(status)
                .build();
    }
}