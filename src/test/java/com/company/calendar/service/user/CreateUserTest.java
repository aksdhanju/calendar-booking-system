package com.company.calendar.service.user;

import com.company.calendar.dto.user.CreateUserRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateUserTest {

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
    @DisplayName("Owner Id already exists in system")
    void testForExistingOwner() {
        var name = "Galen";
        var email = "galensimmons@dealmerridion.com";
        var createUserRequest = CreateUserRequest.builder()
                .id(ownerId)
                .name(name)
                .email(email)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(User.builder().build()));
        var ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(createUserRequest)
        );

        assertEquals("User already exists with email: " + email, ex.getMessage());

        //Scenario 2
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.saveIfAbsent(any())).thenReturn(false);
        var ex2 = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(createUserRequest)
        );
        assertEquals("User already exists in system", ex2.getMessage());
    }

    @Test
    @DisplayName("Owner Id does not exists in system")
    void testForNewUser() {
        var name = "Galen";
        var email = "galensimmons@dealmerridion.com";
        var createUserRequest = CreateUserRequest.builder()
                .id(ownerId)
                .name(name)
                .email(email)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.saveIfAbsent(any())).thenReturn(true);
        var message = userService.createUser(createUserRequest);

        assertEquals("User created successfully with id: 1", message);
    }
}