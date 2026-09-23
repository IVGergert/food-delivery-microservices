package com.gergert.authservice.service.impl;

import com.gergert.authservice.dto.UserMapper;
import com.gergert.authservice.dto.user.*;
import com.gergert.authservice.entity.User;
import com.gergert.authservice.exception.*;
import com.gergert.authservice.kafka.CourierCreatedEventProducer;
import com.gergert.authservice.kafka.CourierUpdatedEventProducer;
import com.gergert.authservice.repository.UserRepository;
import com.gergert.authservice.service.AdminService;
import com.gergert.common.dto.kafka.CourierCreatedEventDto;
import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final CourierCreatedEventProducer courierCreatedEventProducer;
    private final CourierUpdatedEventProducer courierUpdatedEventProducer;

    private final UserMapper mapper;

    @Override
    public List<UserResponseDto> getUsers() {
        log.info("Admin requests list of users");
        return mapper.toUserDto(userRepository.findAll());
    }

    @Override
    public UserResponseDto updateUser(Long userId, AdminUpdateUserRequestDto requestDto) {
        log.info("Admin updates user with userId = {}", userId);

        User user = findUserById(userId);

        if (!user.getEmail().equals(requestDto.email()) && userRepository.existsByEmail(requestDto.email())) {

            log.warn("Admin failed to update user {}: email {} already exists",
                    userId,
                    requestDto.email());

            throw new UserAlreadyExistsException("User with email " + requestDto.email() + " already exists");
        }

        user.setEmail(requestDto.email());
        user.setFirstName(requestDto.firstName());
        user.setLastName(requestDto.lastName());

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.ROLE_COURIER) {
            courierUpdatedEventProducer.send(
                    new CourierUpdatedEventDto(
                            savedUser.getId(),
                            savedUser.getFirstName(),
                            savedUser.getLastName()
                    )
            );
        }

        log.info("User with userId={} updated successfully", userId);

        return mapper.toUserDto(savedUser);
    }

    @Override
    public UserResponseDto changeRole(Long userId, ChangeRoleRequestDto requestDto) {
        log.info("Admin attempts to change role for userId={} to {}",
                userId,
                requestDto.role()
        );

        User user = findUserById(userId);

        Role currentRole = user.getRole();
        Role newRole = requestDto.role();

        if (currentRole == newRole) {
            log.info("User with userId = {} already has role {}",
                    userId,
                    newRole
            );

            return mapper.toUserDto(user);
        }

        if (currentRole == Role.ROLE_COURIER || newRole == Role.ROLE_COURIER) {

            log.warn("Role change involving courier is not available yet. " + "userId={}, currentRole={}, newRole={}",
                    userId,
                    currentRole,
                    newRole
            );

            throw new InvalidRoleChangeException("Role change involving courier is not available yet");
        }

        if (currentRole == Role.ROLE_ADMIN
                && newRole != Role.ROLE_ADMIN
                && userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {

            log.warn("Cannot change role of the last administrator. userId={}", userId);

            throw new LastAdminException("The last administrator cannot lose admin role");
        }

        user.setRole(newRole);

        User savedUser = userRepository.save(user);

        log.info("Role for userId={} changed from {} to {}",
                userId,
                currentRole,
                newRole
        );

        return mapper.toUserDto(savedUser);
    }

    @Override
    public void resetPassword(Long userId, AdminResetPasswordRequestDto requestDto) {
        log.info("Admin resets password for userId={}", userId);

        User user = findUserById(userId);

        if (!requestDto.newPassword().equals(requestDto.confirmPassword())) {
            log.warn("Password reset failed for userId={}: passwords do not match", userId);
            throw new PasswordMismatchException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(requestDto.newPassword()));

        userRepository.save(user);

        log.info("Password for userId={} reset successfully", userId);
    }

    @Override
    public UserResponseDto createAdmin(CreateAdminRequestDto requestDto) {
        log.info("Admin attempts to create admin with email: {}", requestDto.email());

        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            log.warn("Passwords do not match for admin with email: {}", requestDto.email());
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            log.warn("User with email {} already exists", requestDto.email());
            throw new UserAlreadyExistsException("User with email " + requestDto.email() + " already exists");
        }

        User admin = User.builder()
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(Role.ROLE_ADMIN)
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .build();

        User savedAdmin = userRepository.save(admin);

        return mapper.toUserDto(savedAdmin);
    }

    @Override
    public UserResponseDto createCourier(CreateCourierRequestDto requestDto) {

        log.info("Admin attempts to create courier with email: {}", requestDto.email());

        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            log.warn("Passwords do not match for courier with email: {}", requestDto.email());
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            log.warn("User with email already exists: {}", requestDto.email());
            throw new UserAlreadyExistsException("User with email " + requestDto.email() + " already exists");
        }

        User user = User.builder()
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(Role.ROLE_COURIER)
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .build();

        User savedUser = userRepository.save(user);

        CourierCreatedEventDto event = new CourierCreatedEventDto(
                savedUser.getId(),
                savedUser.getEmail(),
                requestDto.firstName(),
                requestDto.lastName(),
                requestDto.transportType()
        );

        courierCreatedEventProducer.send(event);

        log.info("CourierCreatedEvent sent successfully. User ID: {}", savedUser.getId());

        return mapper.toUserDto(savedUser);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with userId " + userId + " not found"
                        )
                );
    }
}
