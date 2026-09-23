package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.UserMapper;
import com.gergert.authservice.dto.user.ChangeEmailRequestDto;
import com.gergert.authservice.dto.user.ChangePasswordRequestDto;
import com.gergert.authservice.dto.user.UpdateProfileRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.InvalidPasswordException;
import com.gergert.authservice.exception.PasswordMismatchException;
import com.gergert.authservice.exception.UserAlreadyExistsException;
import com.gergert.authservice.exception.UserNotFoundException;
import com.gergert.authservice.kafka.CourierUpdatedEventProducer;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    @Mock
    private CourierUpdatedEventProducer courierUpdatedEventProducer;

    @Test
    void getProfile_shouldReturnMappedUser() {
        User user = user(5L, "user@example.com", "first", "last");
        UserResponseDto dto = dto(user);
        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(mapper.toUserDto(user)).thenReturn(dto);

        assertThat(service.getProfile(5L)).isSameAs(dto);
        verify(mapper).toUserDto(user);
    }

    @Test
    void getProfile_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.getProfile(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with userId 99 not found");

        verifyNoInteractions(mapper);
    }

    @Test
    void updateProfile_shouldUpdateNamesAndReturnSavedUser() {
        User user = user(
                5L,
                "user@example.com",
                "Old",
                "Name"
        );

        user.setRole(Role.ROLE_CUSTOMER);

        UserResponseDto dto = dto(user);

        UpdateProfileRequestDto request =
                new UpdateProfileRequestDto(
                        "New",
                        "Surname"
                );

        when(userRepository.findById(5L))
                .thenReturn(java.util.Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(mapper.toUserDto(user))
                .thenReturn(dto);

        assertThat(service.updateProfile(5L, request))
                .isSameAs(dto);

        assertThat(user.getFirstName())
                .isEqualTo("New");

        assertThat(user.getLastName())
                .isEqualTo("Surname");

        verify(userRepository)
                .save(user);

        verify(courierUpdatedEventProducer, never())
                .send(any());
    }

    @Test
    void updateProfile_shouldSendCourierUpdatedEvent_whenUserIsCourier() {
        User user = user(
                5L,
                "courier@example.com",
                "Old",
                "Name"
        );

        user.setRole(Role.ROLE_COURIER);

        UpdateProfileRequestDto request =
                new UpdateProfileRequestDto(
                        "Alex",
                        "Smith"
                );

        UserResponseDto dto = new UserResponseDto(
                5L,
                "courier@example.com",
                "Alex",
                "Smith",
                Role.ROLE_COURIER
        );

        when(userRepository.findById(5L))
                .thenReturn(java.util.Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(mapper.toUserDto(user))
                .thenReturn(dto);

        assertThat(service.updateProfile(5L, request))
                .isSameAs(dto);

        assertThat(user.getFirstName())
                .isEqualTo("Alex");

        assertThat(user.getLastName())
                .isEqualTo("Smith");

        verify(userRepository)
                .save(user);

        verify(courierUpdatedEventProducer)
                .send(new CourierUpdatedEventDto(
                        5L,
                        "Alex",
                        "Smith"
                ));

        verify(mapper)
                .toUserDto(user);
    }

    @Test
    void updateProfile_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(5L))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                service.updateProfile(
                        5L,
                        new UpdateProfileRequestDto(
                                "New",
                                "Name"
                        )
                )
        )
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with userId 5 not found");

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(
                courierUpdatedEventProducer,
                mapper
        );
    }

    @Test
    void changeEmail_shouldChangeEmailWithValidPassword() {
        User user = user(5L, "old@example.com", "First", "Last");
        ChangeEmailRequestDto request = ChangeEmailRequestDto.builder()
                .email("new@example.com")
                .currentPassword("current")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        service.changeEmail(5L, request);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_shouldAllowKeepingCurrentEmail() {
        User user = user(5L, "same@example.com", "First", "Last");
        ChangeEmailRequestDto request = ChangeEmailRequestDto.builder()
                .email("same@example.com")
                .currentPassword("current")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);

        service.changeEmail(5L, request);

        assertThat(user.getEmail()).isEqualTo("same@example.com");
        verify(userRepository).existsByEmail("same@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_shouldRejectInvalidCurrentPassword() {
        User user = user(5L, "old@example.com", "First", "Last");
        ChangeEmailRequestDto request = ChangeEmailRequestDto.builder()
                .email("new@example.com")
                .currentPassword("bad")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.changeEmail(5L, request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Invalid password");

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_shouldRejectExistingEmail() {
        User user = user(5L, "old@example.com", "First", "Last");
        ChangeEmailRequestDto request = ChangeEmailRequestDto.builder()
                .email("taken@example.com")
                .currentPassword("current")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.changeEmail(5L, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email taken@example.com already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldEncodeAndSaveNewPassword() {
        User user = user(5L, "user@example.com", "First", "Last");
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .currentPassword("current")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");

        service.changePassword(5L, request);

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldRejectInvalidCurrentPassword() {
        User user = user(5L, "user@example.com", "First", "Last");
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .currentPassword("bad")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(5L, request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Invalid password");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldRejectMismatchedConfirmation() {
        User user = user(5L, "user@example.com", "First", "Last");
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .currentPassword("current")
                .newPassword("newpass")
                .confirmPassword("different")
                .build();

        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("current", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(5L, request))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("Passwords do not match");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(5L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.changePassword(
                5L,
                ChangePasswordRequestDto.builder()
                        .currentPassword("current")
                        .newPassword("newpass")
                        .confirmPassword("newpass")
                        .build()
        )).isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(passwordEncoder, mapper);
    }

    private User user(Long id, String email, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-old")
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private UserResponseDto dto(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
