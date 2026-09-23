package com.gergert.authservice.exception;

import com.gergert.common.dto.exception.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException() {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                null);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTokenException(InvalidTokenException exception) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException exception) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPasswordException(
            InvalidPasswordException exception) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handlePasswordMismatchException(PasswordMismatchException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null);
    }

    @ExceptionHandler(InvalidRoleChangeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRoleChangeException(InvalidRoleChangeException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }


    @ExceptionHandler(LastAdminException.class)
    public ResponseEntity<ErrorResponseDto> handleLastAdminException(LastAdminException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter: " + exception.getName(),
                null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidJson() {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound() {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException() {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                null);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status,
                                                           String message,
                                                           Map<String, String> errors) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDto(
                        status.value(),
                        message,
                        errors
                ));
    }
}
