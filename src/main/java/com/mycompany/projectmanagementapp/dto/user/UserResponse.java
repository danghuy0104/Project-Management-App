package com.mycompany.projectmanagementapp.dto.user;

import com.mycompany.projectmanagementapp.model.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String email,
        String image,
        Instant createdAt,
        Instant updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getImage(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
