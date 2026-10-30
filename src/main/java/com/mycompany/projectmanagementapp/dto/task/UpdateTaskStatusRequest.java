package com.mycompany.projectmanagementapp.dto.task;

import com.mycompany.projectmanagementapp.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}
