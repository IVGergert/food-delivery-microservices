package com.gergert.authservice.controller;

import com.gergert.authservice.dto.user.ChangeEmailRequestDto;
import com.gergert.authservice.dto.user.ChangePasswordRequestDto;
import com.gergert.authservice.dto.user.UpdateProfileRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.service.UserService;
import com.gergert.common.dto.jwt.JwtClaimsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me/profile")
    public ResponseEntity<UserResponseDto> getProfile(@AuthenticationPrincipal JwtClaimsDto claims) {
        return ResponseEntity.ok(userService.getProfile(claims.userId()));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserResponseDto> putProfile(@AuthenticationPrincipal JwtClaimsDto claims,
                                                      @Valid @RequestBody UpdateProfileRequestDto requestDto) {

        return ResponseEntity.ok(userService.updateProfile(claims.userId(), requestDto));
    }

    @PutMapping("/me/email")
    public ResponseEntity<Void> changeEmail(@AuthenticationPrincipal JwtClaimsDto claims,
                                        @Valid @RequestBody ChangeEmailRequestDto requestDto) {

        userService.changeEmail(claims.userId(), requestDto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal JwtClaimsDto claims,
                                               @Valid @RequestBody ChangePasswordRequestDto requestDto) {

        userService.changePassword(claims.userId(), requestDto);
        return ResponseEntity.noContent().build();
    }
}
