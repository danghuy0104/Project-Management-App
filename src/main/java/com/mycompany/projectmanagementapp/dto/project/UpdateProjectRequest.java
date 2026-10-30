package com.mycompany.projectmanagementapp.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 150) String name,
        @Size(max = 5000) String description,
        ProjectStatus status,
        Priority priority,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @Min(0) @Max(100) Integer progress) {
}
