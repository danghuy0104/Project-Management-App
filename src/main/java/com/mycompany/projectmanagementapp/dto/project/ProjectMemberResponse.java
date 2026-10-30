package com.mycompany.projectmanagementapp.dto.project;

import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.ProjectMember;

public record ProjectMemberResponse(
        String id,
        String userId,
        String projectId,
        UserResponse user) {

    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getProject().getId(),
                UserResponse.from(member.getUser()));
    }
}
