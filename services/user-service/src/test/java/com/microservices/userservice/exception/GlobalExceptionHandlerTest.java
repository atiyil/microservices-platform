package com.microservices.userservice.exception;

import com.microservices.userservice.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/users");
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found with id: 123");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("User not found with id: 123");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleUserAlreadyExistsException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Username already exists: johndoe");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserAlreadyExistsException(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Username already exists: johndoe");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        FieldError fieldError1 = new FieldError("userRequest", "username", "must not be blank");
        FieldError fieldError2 = new FieldError("userRequest", "email", "must be a valid email");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).contains(
                "username: must not be blank",
                "email: must be a valid email"
        );
    }

    @Test
    void shouldHandleValidationExceptionWithSingleError() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        FieldError fieldError = new FieldError("userRequest", "password", "size must be between 8 and 100");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).hasSize(1);
        assertThat(response.getBody().getDetails().get(0)).isEqualTo("password: size must be between 8 and 100");
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected database error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleNullPointerException() {
        Exception exception = new NullPointerException("Null value encountered");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldStripUriPrefixFromPath() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/users/123");

        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception,
                webRequest
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/123");
        assertThat(response.getBody().getPath()).doesNotContain("uri=");
    }

    @Test
    void shouldHandleResourceNotFoundWithDifferentMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Username not found: johndoe");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(
                exception,
                webRequest
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Username not found: johndoe");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldHandleUserAlreadyExistsWithEmailConflict() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Email already exists: test@example.com");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserAlreadyExistsException(
                exception,
                webRequest
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Email already exists: test@example.com");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldHandleValidationExceptionWithNoErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(
                exception,
                webRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isEmpty();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    }

    @Test
    void shouldPreserveOriginalExceptionMessage() {
        String customMessage = "Custom error message with special characters: @#$%";
        Exception exception = new IllegalArgumentException(customMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception,
                webRequest
        );

        // Note: The global handler returns a generic message for security
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("userRequest", "username", "size must be between 3 and 50"),
                new FieldError("userRequest", "email", "must be a well-formed email address"),
                new FieldError("userRequest", "password", "must not be blank"),
                new FieldError("userRequest", "firstName", "must not be null")
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(
                exception,
                webRequest
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).hasSize(4);
        assertThat(response.getBody().getDetails()).containsExactlyInAnyOrder(
                "username: size must be between 3 and 50",
                "email: must be a well-formed email address",
                "password: must not be blank",
                "firstName: must not be null"
        );
    }
}
