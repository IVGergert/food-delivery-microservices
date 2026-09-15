package com.gergert.authservice.service;

import com.gergert.authservice.dto.user.ChangeEmailRequestDto;
import com.gergert.authservice.dto.user.ChangePasswordRequestDto;
import com.gergert.authservice.dto.user.UpdateProfileRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;

public interface UserService {
    UserResponseDto getProfile(Long userId);
    UserResponseDto updateProfile(Long userId, UpdateProfileRequestDto requestDto);
    void changeEmail(Long userId, ChangeEmailRequestDto requestDto);
    void changePassword(Long userId, ChangePasswordRequestDto requestDto);
}
