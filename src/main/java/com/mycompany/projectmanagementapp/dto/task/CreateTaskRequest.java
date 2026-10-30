package com.mycompany.projectmanagementapp.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        TaskType type,
        Priority priority,
        @NotBlank String assigneeId,
        @NotBlank @JsonProperty("due_date") String dueDate) {
}
