package com.gergert.authservice.service;

import com.gergert.authservice.dto.auth.AuthResultDto;
import com.gergert.authservice.dto.auth.LoginRequestDto;
import com.gergert.authservice.dto.auth.RegisterRequestDto;

public interface AuthService {
    AuthResultDto login(LoginRequestDto loginDto);
    AuthResultDto register(RegisterRequestDto registerDto);
    AuthResultDto refresh(String refreshToken);
    void logout(String refreshToken);
}
