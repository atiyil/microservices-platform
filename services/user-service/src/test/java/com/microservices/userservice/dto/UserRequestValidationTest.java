package com.microservices.userservice.dto;

import com.microservices.userservice.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWithValidData() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .role(User.UserRole.USER)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenUsernameIsBlank() {
        UserRequest request = UserRequest.builder()
                .username("")
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");
    }

    @Test
    void shouldFailWhenUsernameIsNull() {
        UserRequest request = UserRequest.builder()
                .username(null)
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");
    }

    @Test
    void shouldFailWhenUsernameTooShort() {
        UserRequest request = UserRequest.builder()
                .username("ab")
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    void shouldFailWhenUsernameTooLong() {
        UserRequest request = UserRequest.builder()
                .username("a".repeat(51))
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    void shouldFailWhenUsernameHasInvalidCharacters() {
        UserRequest request = UserRequest.builder()
                .username("john-doe!")
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Username can only contain letters, numbers, and underscores");
    }

    @Test
    void shouldPassWithUsernameContainingUnderscore() {
        UserRequest request = UserRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email(null)
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("invalid-email")
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email must be valid");
    }

    @Test
    void shouldFailWhenEmailTooLong() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("a".repeat(90) + "@example.com") // Over 100 chars
                .password("SecurePass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenPasswordIsBlank() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
    }

    @Test
    void shouldFailWhenPasswordIsNull() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password(null)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
    }

    @Test
    void shouldFailWhenPasswordTooShort() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Pass1!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
                v.getMessage().contains("Password must be between 8 and 100 characters"));
    }

    @Test
    void shouldFailWhenPasswordTooLong() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("A".repeat(101) + "1!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailWhenPasswordMissingDigit() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must contain at least one digit");
    }

    @Test
    void shouldFailWhenPasswordMissingLowercase() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SECUREPASS123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must contain at least one");
    }

    @Test
    void shouldFailWhenPasswordMissingUppercase() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("securepass123!")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must contain at least one");
    }

    @Test
    void shouldFailWhenPasswordMissingSpecialCharacter() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must contain at least one");
    }

    @Test
    void shouldPassWithAllPasswordRequirements() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Secure123@Pass")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassWhenFirstNameIsNull() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName(null)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenFirstNameTooLong() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .firstName("a".repeat(51))
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("First name must not exceed 50 characters");
    }

    @Test
    void shouldPassWhenLastNameIsNull() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .lastName(null)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenLastNameTooLong() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .lastName("a".repeat(51))
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Last name must not exceed 50 characters");
    }

    @Test
    void shouldPassWithValidPhoneNumber() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .phone("+1 (555) 123-4567")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenPhoneHasInvalidCharacters() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .phone("+1-555-ABC-1234")
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Phone number format is invalid");
    }

    @Test
    void shouldPassWhenPhoneIsNull() {
        UserRequest request = UserRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("SecurePass123!")
                .phone(null)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassWithAllFieldsValid() {
        UserRequest request = UserRequest.builder()
                .username("john_doe123")
                .email("john.doe@example.com")
                .password("VerySecure123@Pass")
                .firstName("John")
                .lastName("Doe")
                .phone("+1 (555) 123-4567")
                .role(User.UserRole.ADMIN)
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHaveMultipleViolationsForCompletelyInvalidRequest() {
        UserRequest request = UserRequest.builder()
                .username("ab")  // Too short
                .email("invalid")  // Invalid format
                .password("weak")  // Too short and missing requirements
                .firstName("a".repeat(51))  // Too long
                .lastName("b".repeat(51))  // Too long
                .phone("invalid-phone")  // Invalid format
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations.size()).isGreaterThan(5);
    }

    @Test
    void shouldPassWithMinimumValidFields() {
        UserRequest request = UserRequest.builder()
                .username("abc")  // Minimum length
                .email("a@b.c")
                .password("Passw0rd!")  // Minimum requirements met
                .build();

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
