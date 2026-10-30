package com.mycompany.projectmanagementapp.dto.auth;

import com.mycompany.projectmanagementapp.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user) {
}
