package com.mycompany.projectmanagementapp.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateProjectRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 5000) String description,
        ProjectStatus status,
        Priority priority,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("team_lead") String teamLead,
        @JsonProperty("team_members") List<String> teamMembers) {
}
