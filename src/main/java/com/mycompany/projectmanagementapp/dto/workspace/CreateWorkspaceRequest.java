package com.mycompany.projectmanagementapp.dto.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String slug,
        @Size(max = 2000) String description,
        @JsonProperty("image_url") @Size(max = 1000) String imageUrl) {
}
