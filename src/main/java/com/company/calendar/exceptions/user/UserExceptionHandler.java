package com.company.calendar.exceptions.user;

import com.company.calendar.dto.user.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(1)
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<UserResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        UserResponse<Object> response = UserResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        UserResponse<Object> response = UserResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
