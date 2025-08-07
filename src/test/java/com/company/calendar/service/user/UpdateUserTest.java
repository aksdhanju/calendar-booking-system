package com.company.calendar.service.user;

import com.company.calendar.dto.user.UpdateUserRequest;
import com.company.calendar.entity.User;
import com.company.calendar.exceptions.user.UserAlreadyExistsException;
import com.company.calendar.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateUserTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private String ownerId;

    @BeforeEach
    void setUp() {
        ownerId = "1";
    }

    @Test
    @DisplayName("Owner Id already exists in system but with different email id")
    void testForExistingOwnerWithDifferentEmailId() {
        var name = "Galen";
        var email = "galensimmons@dealmerridion.com";
        var updateUserRequest = UpdateUserRequest.builder()
                .name(name)
                .email(email)
                .build();
        when(userRepository.findById(anyString())).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.existsByEmailExcludingId(anyString(), anyString())).thenReturn(true);
        var ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(ownerId, updateUserRequest)
        );

        assertEquals("User already exists with email: " + email, ex.getMessage());
    }

    @Test
    @DisplayName("Owner Id does not exists in system")
    void testForNewUser() {
        var name = "Galen";
        var email = "galensimmons@dealmerridion.com";
        var updateUserRequest = UpdateUserRequest.builder()
                .name(name)
                .email(email)
                .build();
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmailExcludingId(anyString(), anyString())).thenReturn(false);
        doNothing().when(userRepository).save(any());
        var result = userService.updateUser(ownerId, updateUserRequest);

        assertEquals("User created successfully with id: " + ownerId, result.getMessage());
        assertFalse(result.isCreated());

        when(userRepository.findById(anyString())).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.existsByEmailExcludingId(anyString(), anyString())).thenReturn(false);
        doNothing().when(userRepository).save(any());
        var resultUpdated = userService.updateUser(ownerId, updateUserRequest);

        assertEquals("User updated successfully with id: " + ownerId, resultUpdated.getMessage());
        assertTrue(resultUpdated.isCreated());
    }
}