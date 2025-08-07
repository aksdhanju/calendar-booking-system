package com.company.calendar.service.user;

import com.company.calendar.dto.user.UpdateUserResult;
import com.company.calendar.dto.user.*;
import com.company.calendar.entity.User;
import com.company.calendar.entity.UserMetadata;
import com.company.calendar.exceptions.user.UserAlreadyExistsException;
import com.company.calendar.exceptions.user.UserNotFoundException;
import com.company.calendar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public String createUser(CreateUserRequest request) {
        var id = request.getId();
        log.info("Attempting to create user with id: {} and email: {}", id, request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        var user = User.builder()
                .id(id)
                .userMetadata(UserMetadata.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .build())
                .build();

        var inserted = userRepository.saveIfAbsent(user);
        if (!inserted) {
            log.warn("User already exists with id: {}", id);
            throw new UserAlreadyExistsException("User already exists with id: " + id);
        }
        var message = "User created successfully with id: {}" + id;
        log.info(message);
        return message;
    }

    public UpdateUserResult updateUser(String id, UpdateUserRequest request) {
        log.info("Attempting to update/create user with id: {}", id);
        boolean alreadyCreated = true;
        if (userRepository.findById(id).isEmpty()) {
            log.warn("User not found with id: {}, will create new user", id);
            alreadyCreated = false;
        }

        if (userRepository.existsByEmailExcludingId(request.getEmail(), id)) {
            log.warn("User already exists with email: {} (excluding id: {})", request.getEmail(), id);
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        var user = User.builder()
                .id(id)
                .userMetadata(UserMetadata.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .build())
                .build();

        //ok with lost updates here
        userRepository.save(user);

        String message;
        if (alreadyCreated) {
            message = "User updated successfully with id: " + id;
        } else {
            message = "User created successfully with id: " + id;
        }
        log.info(message);

        return UpdateUserResult.builder()
                .message(message)
                .created(alreadyCreated)
                .build();
    }

    public String deleteUser(String id) {
        log.info("Attempting to delete user with id: {}", id);
        if (userRepository.findById(id).isEmpty()) {
            log.warn("Cannot delete, user not found with id: {}", id);
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        var message = "User deleted successfully with id: " + id;
        log.info(message);
        return message;
    }

    public Optional<UserResponse<GetUserResponse>> getUser(String id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("User found with id: {}", id);
                    GetUserResponse response = GetUserResponse.builder()
                            .id(user.getId())
                            .name(user.getUserMetadata().getName())
                            .email(user.getUserMetadata().getEmail())
                            .build();

                    return UserResponse.<GetUserResponse>builder()
                            .success(true)
                            .message("User fetched successfully for id: " + id)
                            .data(response)
                            .build();
                });
    }

    public Map<String, User> getUsersByIds(Set<String> ids) {
        log.info("Fetching users by ids: {}", ids);
        var users = userRepository.findByIds(ids);
        log.info("Fetched {} users", users.size());
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    public void validateUserExists(String userId) {
        log.info("Validating existence of user with id: {}", userId);
        if (getUser(userId).isEmpty()) {
            log.warn("User not found during validation with id: {}", userId);
            throw new UserNotFoundException(userId);
        }
        log.info("User exists with id: {}", userId);
    }
}
