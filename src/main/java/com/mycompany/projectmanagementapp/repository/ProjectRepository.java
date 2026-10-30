package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.enums.ProjectStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {

    @EntityGraph(attributePaths = {"owner"})
    List<Project> findByWorkspaceIdOrderByCreatedAtDesc(String workspaceId);

    long countByWorkspaceId(String workspaceId);

    long countByWorkspaceIdAndStatus(String workspaceId, ProjectStatus status);
}
