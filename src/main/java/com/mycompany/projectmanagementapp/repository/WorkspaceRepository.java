package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {

    boolean existsBySlug(String slug);
}
