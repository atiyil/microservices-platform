package com.microservices.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.userservice.dto.UserRequest;
import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void shouldReturnBadRequestForInvalidUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("ab") // Too short
                .email("invalid-email") // Invalid format
                .password("weak") // Too weak
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = createTestUser("testuser", "test@example.com");

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        mockMvc.perform(get("/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void shouldGetUserByUsername() throws Exception {
        createTestUser("johndoe", "john@example.com");

        mockMvc.perform(get("/users/username/{username}", "johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldGetAllUsersWithPagination() throws Exception {
        createTestUser("user1", "user1@example.com");
        createTestUser("user2", "user2@example.com");
        createTestUser("user3", "user3@example.com");

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "username")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldSearchUsers() throws Exception {
        createTestUser("johndoe", "john@example.com");
        createTestUser("janedoe", "jane@example.com");
        createTestUser("bobsmith", "bob@example.com");

        mockMvc.perform(get("/users/search")
                        .param("query", "doe")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = createTestUser("oldusername", "old@example.com");

        UserRequest updateRequest = UserRequest.builder()
                .username("oldusername") // Username cannot be changed
                .email("newemail@example.com")
                .password("NewSecurePass123!")
                .firstName("Updated")
                .lastName("User")
                .phone("+9876543210")
                .build();

        mockMvc.perform(put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void shouldUpdateUserStatus() throws Exception {
        User user = createTestUser("testuser", "test@example.com");

        mockMvc.perform(patch("/users/{id}/status", user.getId())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = createTestUser("deleteuser", "delete@example.com");

        mockMvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        // Verify soft delete
        User deletedUser = userRepository.findById(user.getId()).orElseThrow();
        assert deletedUser.getStatus() == User.UserStatus.DELETED;
    }

    @Test
    void shouldReturnConflictForDuplicateUsername() throws Exception {
        createTestUser("existinguser", "existing@example.com");

        UserRequest duplicateRequest = UserRequest.builder()
                .username("existinguser")
                .email("another@example.com")
                .password("SecurePass123!")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Username already exists")));
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        createTestUser("user1", "duplicate@example.com");

        UserRequest duplicateRequest = UserRequest.builder()
                .username("user2")
                .email("duplicate@example.com")
                .password("SecurePass123!")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email already exists")));
    }

    @Test
    void shouldFailValidationWhenPasswordMissingRequirements() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("weakpass") // Missing uppercase, digit, special char
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[*]").value(hasItem(containsString("Password must contain"))));
    }

    @Test
    void shouldFailValidationWhenUsernameHasInvalidCharacters() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("john-doe!") // Invalid characters
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[*]").value(hasItem(containsString("Username can only contain"))));
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("not-an-email") // Invalid email
                .password("SecurePass123!")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[*]").value(hasItem(containsString("Email"))));
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsMissing() throws Exception {
        UserRequest request = UserRequest.builder()
                .build(); // All required fields missing

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(greaterThanOrEqualTo(3))); // username, email, password
    }

    @Test
    void shouldFailValidationWhenFieldsTooLong() throws Exception {
        UserRequest request = UserRequest.builder()
                .username("a".repeat(51)) // Too long
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("a".repeat(51)) // Too long
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    private User createTestUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .build();
        return userRepository.save(user);
    }
}
