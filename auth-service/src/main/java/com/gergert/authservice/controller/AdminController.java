package com.gergert.authservice.controller;

import com.gergert.authservice.dto.user.*;
import com.gergert.authservice.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId,
                                                      @Valid @RequestBody AdminUpdateUserRequestDto requestDto) {

        return ResponseEntity.ok(adminService.updateUser(userId, requestDto));
    }


    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDto> changeRole(@PathVariable Long userId,
                                                      @Valid @RequestBody ChangeRoleRequestDto requestDto) {

        return ResponseEntity.ok(adminService.changeRole(userId, requestDto));
    }


    @PutMapping("/users/{userId}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long userId,
                                              @Valid @RequestBody AdminResetPasswordRequestDto requestDto) {

        adminService.resetPassword(userId, requestDto);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/users/admins")
    public ResponseEntity<UserResponseDto> createAdmin(@Valid @RequestBody CreateAdminRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.createAdmin(request));
    }

    @PostMapping("/users/couriers")
    public ResponseEntity<UserResponseDto> createCourier(@Valid @RequestBody CreateCourierRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(adminService.createCourier(request));
    }

}
