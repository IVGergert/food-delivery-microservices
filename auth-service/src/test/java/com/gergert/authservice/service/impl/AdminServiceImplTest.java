package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.user.CreateCourierRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.PasswordMismatchException;
import com.gergert.authservice.exception.UserAlreadyExistsException;
import com.gergert.authservice.kafka.CourierCreatedEventProducer;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    CourierCreatedEventProducer producer;

    @InjectMocks
    AdminServiceImpl adminService;

    @Test
    void createCourier_shouldCreateCourierAndPublishEvent() {
        var request = new CreateCourierRequestDto(
                "courier@example.com",
                "password",
                "password",
                "Alex",
                "CAR");

        var saved = User.builder()
                .id(10L)
                .email(request.email())
                .password("encoded")
                .role(Role.ROLE_COURIER)
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponseDto result = adminService.createCourier(request);

        assertThat(result).isEqualTo(new UserResponseDto(
                10L,
                "courier@example.com",
                null,
                null,
                Role.ROLE_COURIER
        ));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor
                .getValue()
                .getRole())
                .isEqualTo(Role.ROLE_COURIER);

        assertThat(userCaptor
                .getValue()
                .getPassword())
                .isEqualTo("encoded");

        verify(producer).send(argThat(event -> event.userId().equals(10L)
                && event.email().equals("courier@example.com")
                && event.name().equals("Alex")
                && event.transportType().equals("CAR")));
    }

    @Test
    void createCourier_shouldRejectPasswordMismatch() {
        var request = new CreateCourierRequestDto(
                "courier@example.com",
                "password",
                "different",
                "Alex",
                "CAR");

        assertThatThrownBy(() -> adminService.createCourier(request)).isInstanceOf(PasswordMismatchException.class);
        verifyNoInteractions(userRepository, passwordEncoder, producer);
    }

    @Test
    void createCourier_shouldRejectExistingEmail() {
        var request = new CreateCourierRequestDto(
                "courier@example.com",
                "password",
                "password",
                "Alex",
                "CAR");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);
        assertThatThrownBy(() -> adminService.createCourier(request)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, producer);
    }

}
