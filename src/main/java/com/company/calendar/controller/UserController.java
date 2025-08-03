package com.company.calendar.controller;

import com.company.calendar.dto.user.*;
import com.company.calendar.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

//Any relevant details about the Invitee or the appointment

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse<Object>> createUser(@RequestBody @Valid CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.builder().success(true).message("User created successfully.").build());
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<UserResponse<Object>> updateUser(@PathVariable @NotBlank String id, @RequestBody @Valid UpdateUserRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(UserResponse.builder().success(true).message("User set successfully.").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse<Object>> deleteUser(@PathVariable @NotBlank String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(UserResponse.builder().success(true).message("User deleted successfully.").build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse<GetUserResponse>> getUser(@PathVariable @NotBlank String id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(UserResponse.<GetUserResponse>builder()
                                .success(false)
                                .message("User not found.")
                                .data(null)
                                .build()));
    }
}