package com.microservices.userservice.dto;

import com.microservices.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.UserStatus status;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
