package com.microservices.userservice.repository;

import com.microservices.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .build();

        testUser2 = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("password456")
                .firstName("Jane")
                .lastName("Doe")
                .phone("+0987654321")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .build();

        testUser3 = User.builder()
                .username("bobsmith")
                .email("bob@example.com")
                .password("password789")
                .firstName("Bob")
                .lastName("Smith")
                .phone("+1122334455")
                .status(User.UserStatus.INACTIVE)
                .role(User.UserRole.USER)
                .build();
    }

    @Test
    void shouldSaveUser() {
        User savedUser = userRepository.save(testUser1);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUsername() {
        userRepository.save(testUser1);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.save(testUser1);

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        userRepository.save(testUser1);

        boolean exists = userRepository.existsByUsername("johndoe");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        userRepository.save(testUser1);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindUsersByStatus() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> activeUsers = userRepository.findByStatus(
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(activeUsers.getContent()).hasSize(2);
        assertThat(activeUsers.getTotalElements()).isEqualTo(2);
        assertThat(activeUsers.getContent())
                .extracting(User::getStatus)
                .containsOnly(User.UserStatus.ACTIVE);
    }

    @Test
    void shouldFindInactiveUsersByStatus() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> inactiveUsers = userRepository.findByStatus(
                User.UserStatus.INACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(inactiveUsers.getContent()).hasSize(1);
        assertThat(inactiveUsers.getContent().get(0).getUsername()).isEqualTo("bobsmith");
    }

    @Test
    void shouldSearchUsersByUsername() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> results = userRepository.searchUsers(
                "doe",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("johndoe", "janedoe");
    }

    @Test
    void shouldSearchUsersByEmail() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> results = userRepository.searchUsers(
                "john@",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldSearchUsersByFirstName() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> results = userRepository.searchUsers(
                "jane",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldSearchUsersByLastName() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        Page<User> results = userRepository.searchUsers(
                "smith",
                User.UserStatus.INACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldSearchBeCaseInsensitive() {
        userRepository.save(testUser1);

        Page<User> results = userRepository.searchUsers(
                "JOHN",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatchesFound() {
        userRepository.save(testUser1);

        Page<User> results = userRepository.searchUsers(
                "nonexistent",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
    }

    @Test
    void shouldOnlyReturnUsersWithMatchingStatus() {
        userRepository.save(testUser1); // ACTIVE
        userRepository.save(testUser3); // INACTIVE - Bob Smith

        Page<User> results = userRepository.searchUsers(
                "bob",
                User.UserStatus.ACTIVE,
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void shouldSupportPagination() {
        for (int i = 1; i <= 5; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@example.com")
                    .password("password")
                    .firstName("User")
                    .lastName("Test" + i)
                    .status(User.UserStatus.ACTIVE)
                    .role(User.UserRole.USER)
                    .build();
            userRepository.save(user);
        }

        Page<User> page1 = userRepository.findAll(PageRequest.of(0, 2));
        Page<User> page2 = userRepository.findAll(PageRequest.of(1, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldSupportSorting() {
        userRepository.save(testUser1); // johndoe
        userRepository.save(testUser2); // janedoe
        userRepository.save(testUser3); // bobsmith

        Page<User> results = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "username"))
        );

        assertThat(results.getContent())
                .extracting(User::getUsername)
                .containsExactly("bobsmith", "janedoe", "johndoe");
    }

    @Test
    void shouldUpdateUser() {
        User savedUser = userRepository.save(testUser1);
        savedUser.setEmail("newemail@example.com");
        savedUser.setFirstName("Johnny");

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("Johnny");
        assertThat(updatedUser.getUpdatedAt()).isAfterOrEqualTo(updatedUser.getCreatedAt());
    }

    @Test
    void shouldDeleteUser() {
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        Optional<User> deleted = userRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldCountUsers() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        long count = userRepository.count();

        assertThat(count).isEqualTo(3);
    }
}
