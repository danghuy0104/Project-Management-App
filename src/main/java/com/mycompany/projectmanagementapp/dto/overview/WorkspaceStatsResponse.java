package com.mycompany.projectmanagementapp.dto.overview;

import java.util.Map;

public record WorkspaceStatsResponse(
        long totalProjects,
        long activeProjects,
        long completedProjects,
        long totalTasks,
        long myTasks,
        long overdueTasks,
        Map<String, Long> tasksByStatus) {
}
