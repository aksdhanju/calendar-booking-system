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

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
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
            throw new UserAlreadyExistsException("User already exists with id: " + id);
        }
        return "User created successfully for id: " + id;
    }

    public UpdateUserResult updateUser(String id, UpdateUserRequest request) {
        boolean created = true;
        if (userRepository.findById(id).isEmpty()) {
            log.warn("User not found with id: {}", id);
            created = false;
        }

        if (userRepository.existsByEmailExcludingId(request.getEmail(), id)) {
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
        return UpdateUserResult.builder()
                .message(created ? "User updated successfully for id: " + id : "User created successfully for id: " + id)
                .created(created)
                .build();
    }

    public String deleteUser(String id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        return "User deleted successfully for id: " + id;
    }

    public Optional<UserResponse<GetUserResponse>> getUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
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
        return userRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    public void validateUserExists(String userId) {
        if (getUser(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }
    }
}
