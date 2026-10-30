package com.mycompany.projectmanagementapp.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.enums.TaskType;

import java.time.Instant;

public record TaskResponse(
        String id,
        String projectId,
        String title,
        String description,
        TaskStatus status,
        TaskType type,
        Priority priority,
        String assigneeId,
        @JsonProperty("due_date") Instant dueDate,
        Instant createdAt,
        Instant updatedAt,
        UserResponse assignee) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getType(),
                task.getPriority(),
                task.getAssignee().getId(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                UserResponse.from(task.getAssignee()));
    }
}
