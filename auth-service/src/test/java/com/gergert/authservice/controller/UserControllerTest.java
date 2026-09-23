package com.gergert.authservice.controller;

import com.gergert.authservice.dto.user.ChangeEmailRequestDto;
import com.gergert.authservice.dto.user.ChangePasswordRequestDto;
import com.gergert.authservice.dto.user.UpdateProfileRequestDto;
import com.gergert.authservice.dto.user.UserResponseDto;
import com.gergert.authservice.service.UserService;
import com.gergert.common.dto.jwt.JwtClaimsDto;
import com.gergert.common.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private final JwtClaimsDto claims = new JwtClaimsDto(42L, Role.ROLE_CUSTOMER);

    @Test
    void getProfile_shouldUseAuthenticatedUserId() {
        UserResponseDto dto = UserResponseDto.builder().userId(42L).email("user@example.com").build();
        when(userService.getProfile(42L)).thenReturn(dto);

        ResponseEntity<UserResponseDto> response = controller.getProfile(claims);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
        verify(userService).getProfile(42L);
    }

    @Test
    void putProfile_shouldUseAuthenticatedUserId() {
        UpdateProfileRequestDto request = new UpdateProfileRequestDto("Ivan", "Petrov");
        UserResponseDto dto = UserResponseDto.builder().userId(42L).email("user@example.com").build();
        when(userService.updateProfile(42L, request)).thenReturn(dto);

        ResponseEntity<UserResponseDto> response = controller.putProfile(claims, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    void changeEmail_shouldReturnNoContent() {
        ChangeEmailRequestDto request = ChangeEmailRequestDto.builder()
                .email("new@example.com")
                .currentPassword("password")
                .build();

        ResponseEntity<Void> response = controller.changeEmail(claims, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).changeEmail(42L, request);
    }

    @Test
    void changePassword_shouldReturnNoContent() {
        ChangePasswordRequestDto request = ChangePasswordRequestDto.builder()
                .currentPassword("password")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();

        ResponseEntity<Void> response = controller.changePassword(claims, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).changePassword(42L, request);
    }
}
