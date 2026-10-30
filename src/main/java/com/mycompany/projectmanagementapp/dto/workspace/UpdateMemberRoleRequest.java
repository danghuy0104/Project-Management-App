package com.mycompany.projectmanagementapp.dto.workspace;

import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRoleRequest(@NotBlank String role) {
}
