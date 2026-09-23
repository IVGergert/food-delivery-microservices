package com.gergert.authservice.service;

import com.gergert.authservice.dto.user.*;

import java.util.List;

public interface AdminService {
    List<UserResponseDto> getUsers();

    UserResponseDto updateUser(Long userId, AdminUpdateUserRequestDto requestDto);
    UserResponseDto changeRole(Long userId, ChangeRoleRequestDto requestDto);

    void resetPassword(Long userId, AdminResetPasswordRequestDto requestDto);

    UserResponseDto createAdmin(CreateAdminRequestDto requestDto);
    UserResponseDto createCourier(CreateCourierRequestDto requestDto);

}
