package com.mycompany.projectmanagementapp.dto.task;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkDeleteTasksRequest(@NotEmpty List<String> ids) {
}
