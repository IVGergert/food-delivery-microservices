package com.gergert.authservice.service;

import com.gergert.authservice.dto.user.CreateCourierRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;

public interface AdminService {
    UserResponseDto createCourier(CreateCourierRequestDto request);

}
