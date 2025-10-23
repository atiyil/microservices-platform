package com.microservices.userservice.service;

import com.microservices.userservice.dto.UserRequest;
import com.microservices.userservice.dto.UserResponse;
import com.microservices.userservice.exception.ResourceNotFoundException;
import com.microservices.userservice.exception.UserAlreadyExistsException;
import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest validRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .build();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.createUser(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldGetUserByUsernameSuccessfully() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserByUsername("johndoe");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UserRequest updateRequest = UserRequest.builder()
                .username("johndoe")
                .email("newemail@example.com")
                .password("NewPass123!")
                .firstName("John")
                .lastName("Updated")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).save(argThat(user -> 
            user.getStatus() == User.UserStatus.DELETED
        ));
    }

    @Test
    void shouldUpdateUserStatusSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUserStatus(1L, User.UserStatus.INACTIVE);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }
}
