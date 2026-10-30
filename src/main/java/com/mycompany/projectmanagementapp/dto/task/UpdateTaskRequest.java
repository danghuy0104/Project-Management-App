package com.mycompany.projectmanagementapp.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.enums.TaskType;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        TaskType type,
        Priority priority,
        String assigneeId,
        @JsonProperty("due_date") String dueDate) {
}
