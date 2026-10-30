package com.mycompany.projectmanagementapp.dto.overview;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceMemberResponse;
import com.mycompany.projectmanagementapp.model.Workspace;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;

import java.time.Instant;
import java.util.List;

public record WorkspaceOverviewResponse(
        String id,
        String name,
        String slug,
        String description,
        @JsonRawValue String settings,
        String ownerId,
        @JsonProperty("image_url") String imageUrl,
        Instant createdAt,
        Instant updatedAt,
        WorkspaceRole role,
        List<WorkspaceMemberResponse> members,
        List<ProjectOverviewResponse> projects) {

    public static WorkspaceOverviewResponse from(Workspace workspace,
                                                 WorkspaceRole role,
                                                 List<WorkspaceMemberResponse> members,
                                                 List<ProjectOverviewResponse> projects) {
        return new WorkspaceOverviewResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getDescription(),
                workspace.getSettings(),
                workspace.getOwner().getId(),
                workspace.getImageUrl(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt(),
                role,
                members,
                projects);
    }
}
