package com.gergert.authservice.controller;

import com.gergert.authservice.dto.user.*;
import com.gergert.authservice.service.AdminService;
import com.gergert.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController controller;

    @Test
    void getUsers_shouldReturnOk() {
        UserResponseDto user = user(1L, Role.ROLE_CUSTOMER);
        when(adminService.getUsers()).thenReturn(List.of(user));

        ResponseEntity<List<UserResponseDto>> response = controller.getUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(user);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        var request = new AdminUpdateUserRequestDto(
                "new@example.com",
                "Ivan",
                "Petrov");
        var user = user(1L, Role.ROLE_CUSTOMER);
        when(adminService.updateUser(1L, request)).thenReturn(user);

        ResponseEntity<UserResponseDto> response = controller.updateUser(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(user);
    }

    @Test
    void changeRole_shouldReturnUpdatedUser() {
        var request = new ChangeRoleRequestDto(Role.ROLE_ADMIN);
        var user = user(1L, Role.ROLE_ADMIN);
        when(adminService.changeRole(1L, request)).thenReturn(user);

        ResponseEntity<UserResponseDto> response = controller.changeRole(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(user);
    }

    @Test
    void resetPassword_shouldReturnNoContent() {
        var request = new AdminResetPasswordRequestDto(
                "password",
                "password");

        ResponseEntity<Void> response = controller.resetPassword(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(adminService).resetPassword(1L, request);
    }

    @Test
    void createAdmin_shouldReturnCreated() {
        var request = new CreateAdminRequestDto(
                "admin@example.com",
                "password",
                "password",
                "Ivan",
                "Petrov"
        );
        var user = user(2L, Role.ROLE_ADMIN);
        when(adminService.createAdmin(request)).thenReturn(user);

        ResponseEntity<UserResponseDto> response = controller.createAdmin(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(user);
    }

    @Test
    void createCourier_shouldReturnCreated() {
        var request = new CreateCourierRequestDto(
                "courier@example.com",
                "password",
                "password",
                "Ivan",
                "Petrov",
                "CAR"
        );
        var user = user(3L, Role.ROLE_COURIER);
        when(adminService.createCourier(request)).thenReturn(user);

        ResponseEntity<UserResponseDto> response = controller.createCourier(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(user);
    }

    private UserResponseDto user(Long id, Role role) {
        return UserResponseDto.builder()
                .userId(id)
                .email("user" + id + "@example.com")
                .firstName("First")
                .lastName("Last")
                .role(role)
                .build();
    }
}
