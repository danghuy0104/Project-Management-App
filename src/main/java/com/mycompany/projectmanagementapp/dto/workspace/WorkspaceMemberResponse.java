package com.mycompany.projectmanagementapp.dto.workspace;

import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;

public record WorkspaceMemberResponse(
        String id,
        String userId,
        String workspaceId,
        String message,
        WorkspaceRole role,
        UserResponse user) {

    public static WorkspaceMemberResponse from(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getWorkspace().getId(),
                member.getMessage(),
                member.getRole(),
                UserResponse.from(member.getUser()));
    }
}
