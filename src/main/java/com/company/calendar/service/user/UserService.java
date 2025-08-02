package com.company.calendar.service.user;

import com.company.calendar.dto.user.*;
import com.company.calendar.entity.User;
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

    public void createUser(CreateUserRequest request) {
        if (userRepository.findById(request.getId()).isPresent()) {
            throw new UserAlreadyExistsException(request.getId());
        }
        var user = User.builder()
                .id(request.getId())
                .name(request.getName())
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public void updateUser(String id, UpdateUserRequest request) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        var user = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public void deleteUser(String id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public Optional<UserResponse<GetUserResponse>> getUser(String id) {
        return userRepository.findById(id)
                .map(user -> {
                    GetUserResponse response = GetUserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .build();

                    return UserResponse.<GetUserResponse>builder()
                            .success(true)
                            .message("User fetched successfully.")
                            .data(response)
                            .build();
                });
    }

    public Map<String, GetUserResponse> getUsersByIds(Set<String> ids) {
        return userRepository.findByIds(ids).stream()
                .map(user -> GetUserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toMap(GetUserResponse::getId, u -> u));
    }
}
