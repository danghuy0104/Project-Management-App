package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.ProjectMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {

    @EntityGraph(attributePaths = {"user"})
    List<ProjectMember> findByProjectId(String projectId);

    @EntityGraph(attributePaths = {"user"})
    List<ProjectMember> findByProjectWorkspaceId(String workspaceId);

    Optional<ProjectMember> findByIdAndProjectId(String id, String projectId);

    boolean existsByProjectIdAndUserId(String projectId, String userId);

    /** Dùng khi một thành viên rời/bị xoá khỏi workspace. Phải gọi trong transaction. */
    void deleteByProjectWorkspaceIdAndUserId(String workspaceId, String userId);
}
