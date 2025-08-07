package com.company.calendar.controller;

import com.company.calendar.dto.user.*;
import com.company.calendar.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse<Object>> createUser(@RequestBody @Valid CreateUserRequest request) {
        log.info("Received request to create user with id: {}, email: {}, name: {}", request.getId(), request.getEmail(), request.getName());
        var message = userService.createUser(request);
        log.info(message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.builder().success(true).message(message).build());
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<UserResponse<Object>> updateUser(@PathVariable
                                                           @NotBlank(message = "Id should not be blank")
                                                           @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Id can only contain letters, digits, hyphens, and underscores")
                                                           @Size(max = 64, message = "Id must be between 1 and 64 characters")
                                                           String id,
                                                           @RequestBody
                                                           @Valid
                                                           UpdateUserRequest request) {
        log.info("Received request to create user with id: {}, email: {}, name: {}", id, request.getEmail(), request.getName());
        var updateUserResult = userService.updateUser(id, request);
        log.info(updateUserResult.getMessage());
        return ResponseEntity
                .status(updateUserResult.isCreated() ? HttpStatus.OK: HttpStatus.CREATED)
                .body(UserResponse.builder()
                        .success(true)
                        .message(updateUserResult.getMessage())
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse<Object>> deleteUser(@PathVariable
                                                           @NotBlank(message = "Id should not be blank")
                                                           @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Id can only contain letters, digits, hyphens, and underscores")
                                                           @Size(max = 64, message = "Id must be between 1 and 64 characters")
                                                           String id) {
        log.info("Received request to delete user with id: {}", id);
        var message = userService.deleteUser(id);
        log.info(message);
        return ResponseEntity.ok(UserResponse.builder().success(true).message(message).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse<GetUserResponse>> getUser(@PathVariable
                                                                 @NotBlank(message = "Id should not be blank")
                                                                 @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Id can only contain letters, digits, hyphens, and underscores")
                                                                 @Size(max = 64, message = "Id must be between 1 and 64 characters")
                                                                 String id) {
        log.info("Fetching user details for id: {}", id);
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(UserResponse.<GetUserResponse>builder()
                                .success(false)
                                .message("User not found for id: " + id)
                                .data(null)
                                .build()));
    }
}