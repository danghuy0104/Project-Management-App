package com.mycompany.projectmanagementapp.dto.overview;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.dto.project.ProjectMemberResponse;
import com.mycompany.projectmanagementapp.dto.task.TaskResponse;
import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.model.ProjectMember;
import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.ProjectStatus;

import java.time.Instant;
import java.util.List;

public record ProjectOverviewResponse(
        String id,
        String name,
        String description,
        Priority priority,
        ProjectStatus status,
        @JsonProperty("start_date") Instant startDate,
        @JsonProperty("end_date") Instant endDate,
        @JsonProperty("team_lead") String teamLead,
        String workspaceId,
        int progress,
        Instant createdAt,
        Instant updatedAt,
        UserResponse owner,
        List<ProjectMemberResponse> members,
        List<TaskResponse> tasks) {

    public static ProjectOverviewResponse from(Project project, List<ProjectMember> members, List<Task> tasks) {
        List<ProjectMemberResponse> memberResponses = members == null
                ? List.of()
                : members.stream().map(ProjectMemberResponse::from).toList();
        List<TaskResponse> taskResponses = tasks == null
                ? List.of()
                : tasks.stream().map(TaskResponse::from).toList();
        return new ProjectOverviewResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getPriority(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                project.getOwner().getId(),
                project.getWorkspace().getId(),
                project.getProgress(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                UserResponse.from(project.getOwner()),
                memberResponses,
                taskResponses);
    }
}
