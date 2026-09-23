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
import com.gergert.authservice.service.UserService;
import com.gergert.common.dto.kafka.CourierUpdatedEventDto;
import com.gergert.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    private final CourierUpdatedEventProducer courierUpdatedEventProducer;

    @Override
    public UserResponseDto getProfile(Long userId) {
        log.info("Getting profile for userId={}", userId);

        User user = findUserById(userId);
        return mapper.toUserDto(user);
    }

    @Override
    public UserResponseDto updateProfile(Long userId, UpdateProfileRequestDto requestDto) {
        log.info("Updating profile for userId={}", userId);
        User user = findUserById(userId);

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

        log.info("Profile updated for userId={}", userId);

        return mapper.toUserDto(savedUser);
    }

    @Override
    public void changeEmail(Long userId, ChangeEmailRequestDto requestDto) {
        log.info("Changing email for userId={}", userId);
        User user = findUserById(userId);

        if (!passwordEncoder.matches(requestDto.currentPassword(), user.getPassword())) {
            log.warn("Invalid password when changing email for userId={}", userId);
            throw new InvalidPasswordException("Invalid password");
        }

        if (userRepository.existsByEmail(requestDto.email()) && !user.getEmail().equals(requestDto.email())) {
            log.warn("Email change failed: email already exists for userId={}", userId);
            throw new UserAlreadyExistsException("User with email " + requestDto.email() + " already exists");
        }

        user.setEmail(requestDto.email());
        userRepository.save(user);
        log.info("Email changed for userId={}", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDto requestDto) {
        log.info("Changing password for userId={}", userId);
        User user = findUserById(userId);

        if (!passwordEncoder.matches(requestDto.currentPassword(), user.getPassword())) {
            log.warn("Invalid password when changing password for userId={}", userId);
            throw new InvalidPasswordException("Invalid password");
        }

        if (!requestDto.newPassword().equals(requestDto.confirmPassword())) {
            log.warn("Password confirmation failed for userId={}", userId);
            throw new PasswordMismatchException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(requestDto.newPassword()));

        userRepository.save(user);
        log.info("Password changed for userId={}", userId);
    }


    private User findUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with userId " + userId + " not found"));

    }
}
