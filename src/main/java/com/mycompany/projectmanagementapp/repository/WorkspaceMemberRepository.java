package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    Optional<WorkspaceMember> findByIdAndWorkspaceId(String id, String workspaceId);

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    @EntityGraph(attributePaths = {"user"})
    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    @EntityGraph(attributePaths = {"workspace", "workspace.owner"})
    List<WorkspaceMember> findByUserId(String userId);
}
