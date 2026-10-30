package com.mycompany.projectmanagementapp.dto.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.mycompany.projectmanagementapp.model.Workspace;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;

import java.time.Instant;

public record WorkspaceResponse(
        String id,
        String name,
        String slug,
        String description,
        @JsonRawValue String settings,
        String ownerId,
        @JsonProperty("image_url") String imageUrl,
        Instant createdAt,
        Instant updatedAt,
        WorkspaceRole role) {

    public static WorkspaceResponse from(Workspace workspace, WorkspaceRole role) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getDescription(),
                workspace.getSettings(),
                workspace.getOwner().getId(),
                workspace.getImageUrl(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt(),
                role);
    }
}
