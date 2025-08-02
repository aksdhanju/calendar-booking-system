package com.company.calendar.service.user;

import com.company.calendar.dto.*;
import com.company.calendar.entity.User;
import com.company.calendar.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void createUser(CreateUserRequest request) {
        var user = User.builder()
                .id(request.getId())
                .name(request.getName())
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public void updateUser(String id, UpdateUserRequest request) {
        var user = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .build();
        userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public UserResponse getUser(String id) {
        return userRepository.findById(id)
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .build())
                .orElse(null);
    }

    public List<UserResponse> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .build())
                .collect(toList());
    }
}
