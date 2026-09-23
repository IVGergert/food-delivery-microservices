package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.UserMapper;
import com.gergert.authservice.dto.user.AdminResetPasswordRequestDto;
import com.gergert.authservice.dto.user.AdminUpdateUserRequestDto;
import com.gergert.authservice.dto.user.ChangeRoleRequestDto;
import com.gergert.authservice.dto.user.CreateAdminRequestDto;
import com.gergert.authservice.dto.user.CreateCourierRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.InvalidRoleChangeException;
import com.gergert.authservice.exception.LastAdminException;
import com.gergert.authservice.exception.PasswordMismatchException;
import com.gergert.authservice.exception.UserAlreadyExistsException;
import com.gergert.authservice.exception.UserNotFoundException;
import com.gergert.authservice.kafka.CourierCreatedEventProducer;
import com.gergert.authservice.kafka.CourierUpdatedEventProducer;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.common.dto.kafka.CourierCreatedEventDto;
import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CourierCreatedEventProducer courierCreatedEventProducer;

    @Mock
    private CourierUpdatedEventProducer courierUpdatedEventProducer;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getUsers_shouldReturnMappedUsers() {
        User first = user(
                1L,
                "one@example.com",
                Role.ROLE_CUSTOMER
        );

        User second = user(
                2L,
                "two@example.com",
                Role.ROLE_ADMIN
        );

        UserResponseDto firstDto = dto(first);
        UserResponseDto secondDto = dto(second);

        when(userRepository.findAll())
                .thenReturn(List.of(first, second));

        when(mapper.toUserDto(List.of(first, second)))
                .thenReturn(List.of(firstDto, secondDto));

        assertThat(adminService.getUsers())
                .containsExactly(firstDto, secondDto);

        verify(mapper)
                .toUserDto(List.of(first, second));
    }

    @Test
    void updateUser_shouldUpdateDataWhenEmailIsAvailable() {
        User user = user(
                7L,
                "old@example.com",
                Role.ROLE_CUSTOMER
        );

        AdminUpdateUserRequestDto request =
                new AdminUpdateUserRequestDto(
                        "new@example.com",
                        "Ivan",
                        "Petrov"
                );

        UserResponseDto response = dto(user);

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        when(mapper.toUserDto(user))
                .thenReturn(response);

        assertThat(adminService.updateUser(7L, request))
                .isSameAs(response);

        assertThat(user.getEmail())
                .isEqualTo("new@example.com");

        assertThat(user.getFirstName())
                .isEqualTo("Ivan");

        assertThat(user.getLastName())
                .isEqualTo("Petrov");

        verify(userRepository)
                .save(user);

        verify(courierUpdatedEventProducer, never())
                .send(any());
    }

    @Test
    void updateUser_shouldAllowKeepingSameEmail() {
        User user = user(
                7L,
                "same@example.com",
                Role.ROLE_CUSTOMER
        );

        AdminUpdateUserRequestDto request = new AdminUpdateUserRequestDto(
                        "same@example.com",
                        "Ivan",
                        "Petrov"
                );

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(mapper.toUserDto(user))
                .thenReturn(dto(user));

        adminService.updateUser(7L, request);

        verify(userRepository, never())
                .existsByEmail("same@example.com");

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateUser_shouldRejectExistingEmail() {
        User user = user(
                7L,
                "old@example.com",
                Role.ROLE_CUSTOMER
        );

        AdminUpdateUserRequestDto request = new AdminUpdateUserRequestDto(
                        "taken@example.com",
                        "Ivan",
                        "Petrov"
                );

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("taken@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(7L, request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email taken@example.com already exists");

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(mapper);
    }

    @Test
    void updateUser_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adminService.updateUser(99L, new AdminUpdateUserRequestDto(
                                "new@example.com",
                                "Ivan",
                                "Petrov"
                        )
                )
        ).isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with userId 99 not found");

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void changeRole_shouldReturnUserWithoutSaveWhenRoleIsAlreadyTheSame() {
        User user = user(
                10L,
                "admin@example.com",
                Role.ROLE_ADMIN
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(mapper.toUserDto(user))
                .thenReturn(dto(user));

        UserResponseDto result = adminService.changeRole(
                        10L,
                        new ChangeRoleRequestDto(Role.ROLE_ADMIN)
                );

        assertThat(result)
                .isNotNull();

        verify(userRepository, never())
                .save(any());

        verify(userRepository, never())
                .countByRole(any());
    }

    @Test
    void changeRole_shouldChangeCustomerToAdmin() {
        User user = user(
                10L,
                "user@example.com",
                Role.ROLE_CUSTOMER
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(mapper.toUserDto(user))
                .thenReturn(dto(user));

        adminService.changeRole(10L, new ChangeRoleRequestDto(Role.ROLE_ADMIN));

        assertThat(user.getRole())
                .isEqualTo(Role.ROLE_ADMIN);

        verify(userRepository)
                .save(user);
    }

    @Test
    void changeRole_shouldRejectAnyChangeInvolvingCourier() {
        User courier = user(
                10L,
                "courier@example.com",
                Role.ROLE_COURIER
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(courier));

        assertThatThrownBy(
                () -> adminService.changeRole(10L, new ChangeRoleRequestDto(Role.ROLE_CUSTOMER))
        )
                .isInstanceOf(InvalidRoleChangeException.class)
                .hasMessage("Role change involving courier is not available yet");

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void changeRole_shouldRejectPromotingCustomerToCourier() {
        User customer = user(
                10L,
                "user@example.com",
                Role.ROLE_CUSTOMER
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(
                () -> adminService.changeRole(10L, new ChangeRoleRequestDto(Role.ROLE_COURIER)))
                .isInstanceOf(InvalidRoleChangeException.class);

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void changeRole_shouldRejectRemovingRoleFromLastAdmin() {
        User admin = user(
                10L,
                "admin@example.com",
                Role.ROLE_ADMIN
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(admin));

        when(userRepository.countByRole(Role.ROLE_ADMIN))
                .thenReturn(1L);

        assertThatThrownBy(
                () -> adminService.changeRole(10L, new ChangeRoleRequestDto(Role.ROLE_CUSTOMER)))
                .isInstanceOf(LastAdminException.class)
                .hasMessage("The last administrator cannot lose admin role");

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void changeRole_shouldAllowRemovingAdminRoleWhenAnotherAdminExists() {
        User admin = user(
                10L,
                "admin@example.com",
                Role.ROLE_ADMIN
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(admin));

        when(userRepository.countByRole(Role.ROLE_ADMIN))
                .thenReturn(2L);

        when(userRepository.save(admin))
                .thenReturn(admin);

        when(mapper.toUserDto(admin))
                .thenReturn(dto(admin));

        adminService.changeRole(10L, new ChangeRoleRequestDto(Role.ROLE_CUSTOMER));

        assertThat(admin.getRole())
                .isEqualTo(Role.ROLE_CUSTOMER);

        verify(userRepository)
                .save(admin);
    }

    @Test
    void resetPassword_shouldEncodeAndSavePassword() {
        User user = user(
                8L,
                "user@example.com",
                Role.ROLE_CUSTOMER
        );

        AdminResetPasswordRequestDto request =
                new AdminResetPasswordRequestDto(
                        "newpass",
                        "newpass"
                );

        when(userRepository.findById(8L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newpass"))
                .thenReturn("encoded");

        adminService.resetPassword(8L, request);

        assertThat(user.getPassword())
                .isEqualTo("encoded");

        verify(userRepository)
                .save(user);
    }

    @Test
    void resetPassword_shouldRejectMismatchedPasswords() {
        User user = user(
                8L,
                "user@example.com",
                Role.ROLE_CUSTOMER
        );

        when(userRepository.findById(8L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(
                () -> adminService.resetPassword(8L,
                        new AdminResetPasswordRequestDto("newpass", "different")))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("Passwords do not match");

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void resetPassword_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(8L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adminService.resetPassword(8L, new AdminResetPasswordRequestDto(
                                "newpass",
                                "newpass"
                        )
                )
        ).isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createAdmin_shouldCreateAdminWithEncodedPassword() {
        CreateAdminRequestDto request =
                new CreateAdminRequestDto(
                        "admin@example.com",
                        "password",
                        "password",
                        "Ivan",
                        "Petrov"
                );

        User saved = user(20L, "admin@example.com", Role.ROLE_ADMIN);

        saved.setFirstName("Ivan");
        saved.setLastName("Petrov");

        UserResponseDto response = dto(saved);

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(saved);

        when(mapper.toUserDto(saved))
                .thenReturn(response);

        assertThat(adminService.createAdmin(request)).isSameAs(response);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(captor.capture());

        User created = captor.getValue();

        assertThat(created.getRole())
                .isEqualTo(Role.ROLE_ADMIN);

        assertThat(created.getEmail())
                .isEqualTo(request.email());

        assertThat(created.getPassword())
                .isEqualTo("encoded");

        assertThat(created.getFirstName())
                .isEqualTo("Ivan");

        assertThat(created.getLastName())
                .isEqualTo("Petrov");

        verifyNoInteractions(
                courierCreatedEventProducer
        );
    }

    @Test
    void createAdmin_shouldRejectMismatchedPasswords() {
        CreateAdminRequestDto request = new CreateAdminRequestDto(
                        "admin@example.com",
                        "password",
                        "different",
                        "Ivan",
                        "Petrov"
                );

        assertThatThrownBy(() -> adminService.createAdmin(request)).isInstanceOf(PasswordMismatchException.class);

        verifyNoInteractions(
                userRepository,
                passwordEncoder,
                mapper,
                courierCreatedEventProducer
        );
    }

    @Test
    void createAdmin_shouldRejectExistingEmail() {
        CreateAdminRequestDto request = new CreateAdminRequestDto(
                        "admin@example.com",
                        "password",
                        "password",
                        "Ivan",
                        "Petrov"
                );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email admin@example.com already exists");

        verify(userRepository)
                .existsByEmail(request.email());

        verifyNoInteractions(
                passwordEncoder,
                mapper,
                courierCreatedEventProducer
        );
    }

    @Test
    void createCourier_shouldThrowException_whenEmailAlreadyExists() {
        CreateCourierRequestDto requestDto = courierRequest(
                        "courier@example.com",
                        "password123",
                        "password123",
                        "Alex",
                        "Smith",
                        "CAR"
                );

        when(userRepository.existsByEmail(requestDto.email()))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.createCourier(requestDto))
                .isInstanceOf(
                        UserAlreadyExistsException.class
                );

        verify(userRepository)
                .existsByEmail(requestDto.email());

        verifyNoInteractions(
                passwordEncoder,
                courierCreatedEventProducer,
                mapper
        );
    }

    @Test
    void createCourier_shouldThrowException_whenPasswordsDoNotMatch() {
        CreateCourierRequestDto requestDto = courierRequest(
                        "courier@example.com",
                        "password123",
                        "different123"
                );

        assertThatThrownBy(() -> adminService.createCourier(requestDto)
        ).isInstanceOf(PasswordMismatchException.class);

        verifyNoInteractions(
                userRepository,
                passwordEncoder,
                courierCreatedEventProducer,
                mapper
        );
    }

    @Test
    void createCourier_shouldCreateCourierAndPublishEvent() {
        CreateCourierRequestDto requestDto = courierRequest(
                "courier@example.com",
                "password123",
                "password123",
                "Ivan",
                "Petrov",
                "CAR"
        );

        User savedUser = user(
                10L,
                "courier@example.com",
                Role.ROLE_COURIER
        );

        UserResponseDto responseDto = dto(savedUser);

        when(userRepository.existsByEmail(requestDto.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(requestDto.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(mapper.toUserDto(savedUser))
                .thenReturn(responseDto);

        assertThat(adminService.createCourier(requestDto))
                .isEqualTo(responseDto);

        // Проверяем, что имя и фамилия действительно сохраняются в User
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User createdUser = userCaptor.getValue();

        assertThat(createdUser.getId())
                .isNull();

        assertThat(createdUser.getEmail())
                .isEqualTo("courier@example.com");

        assertThat(createdUser.getRole())
                .isEqualTo(Role.ROLE_COURIER);

        assertThat(createdUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(createdUser.getFirstName())
                .isEqualTo("Ivan");

        assertThat(createdUser.getLastName())
                .isEqualTo("Petrov");

        // Проверяем Kafka event
        ArgumentCaptor<CourierCreatedEventDto> eventCaptor =
                ArgumentCaptor.forClass(CourierCreatedEventDto.class);

        verify(courierCreatedEventProducer)
                .send(eventCaptor.capture());

        CourierCreatedEventDto event =
                eventCaptor.getValue();

        assertThat(event.userId())
                .isEqualTo(10L);

        assertThat(event.email())
                .isEqualTo("courier@example.com");

        assertThat(event.firstName())
                .isEqualTo("Ivan");

        assertThat(event.lastName())
                .isEqualTo("Petrov");

        assertThat(event.transportType())
                .isEqualTo("CAR");

        verifyNoInteractions(courierUpdatedEventProducer);
    }

    @Test
    void findUser_shouldThrowForMissingUserAcrossOperations() {
        when(userRepository.findById(404L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.changeRole(404L, new ChangeRoleRequestDto(Role.ROLE_ADMIN)))
                .isInstanceOf(UserNotFoundException.class);

        assertThatThrownBy(() -> adminService.resetPassword(404L,
                new AdminResetPasswordRequestDto("newpass", "newpass"))
        ).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_shouldSendCourierUpdatedEvent_whenUserIsCourier() {
        User user = User.builder()
                .id(10L)
                .email("old@example.com")
                .firstName("Ivan")
                .lastName("Petrov")
                .role(Role.ROLE_COURIER)
                .build();

        AdminUpdateUserRequestDto request = new AdminUpdateUserRequestDto(
                "new@example.com",
                "Alex",
                "Smith"
        );

        User savedUser = User.builder()
                .id(10L)
                .email("new@example.com")
                .firstName("Alex")
                .lastName("Smith")
                .role(Role.ROLE_COURIER)
                .build();

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        adminService.updateUser(10L, request);

        verify(courierUpdatedEventProducer).send(new CourierUpdatedEventDto(
                        10L,
                        "Alex",
                        "Smith"
                )
        );
    }

    @Test
    void updateUser_shouldNotSendCourierUpdatedEvent_whenUserIsNotCourier() {
        User user = User.builder()
                .id(10L)
                .email("old@example.com")
                .firstName("Ivan")
                .lastName("Petrov")
                .role(Role.ROLE_CUSTOMER)
                .build();

        AdminUpdateUserRequestDto request = new AdminUpdateUserRequestDto(
                "new@example.com",
                "Alex",
                "Smith"
        );

        User savedUser = User.builder()
                .id(10L)
                .email("new@example.com")
                .firstName("Alex")
                .lastName("Smith")
                .role(Role.ROLE_CUSTOMER)
                .build();

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        adminService.updateUser(10L, request);

        verify(courierUpdatedEventProducer, never()).send(any());
    }

    private CreateCourierRequestDto courierRequest(String email,
                                                   String password,
                                                   String confirmPassword) {

        return new CreateCourierRequestDto(
                email,
                password,
                confirmPassword,
                "Ivan",
                "Petrov",
                "CAR"
        );
    }

    private CreateCourierRequestDto courierRequest(String email,
                                                   String password,
                                                   String confirmPassword,
                                                   String firstName,
                                                   String lastName,
                                                   String transportType) {

        return new CreateCourierRequestDto(
                email,
                password,
                confirmPassword,
                firstName,
                lastName,
                transportType
        );
    }

    private User user(Long id,
                      String email,
                      Role role) {

        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-old")
                .firstName("First")
                .lastName("Last")
                .role(role)
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