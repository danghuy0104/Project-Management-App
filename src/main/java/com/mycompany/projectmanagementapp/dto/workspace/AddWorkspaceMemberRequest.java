package com.mycompany.projectmanagementapp.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddWorkspaceMemberRequest(
        @NotBlank @Email String email,
        String role,
        @Size(max = 500) String message) {
}
