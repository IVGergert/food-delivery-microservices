package com.gergert.authservice.service;

import com.gergert.authservice.dto.*;

public interface AuthService {
    AuthResultDto login(LoginRequestDto loginDto);
    AuthResultDto register(RegisterRequestDto registerDto);
    AuthResultDto refresh(String refreshToken);
    void logout(String refreshToken);
}
