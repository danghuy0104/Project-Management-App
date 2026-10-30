package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByProjectIdOrderByCreatedAtDesc(String projectId);

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByProjectWorkspaceId(String workspaceId);

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByProjectWorkspaceIdAndAssigneeId(String workspaceId, String assigneeId);

    @EntityGraph(attributePaths = {"project"})
    List<Task> findByIdIn(Collection<String> ids);

    long countByProjectWorkspaceId(String workspaceId);

    long countByProjectWorkspaceIdAndStatus(String workspaceId, TaskStatus status);

    long countByProjectWorkspaceIdAndAssigneeId(String workspaceId, String assigneeId);

    @Query("select count(t) from Task t where t.project.workspace.id = :workspaceId "
            + "and t.status <> :doneStatus and t.dueDate < :now")
    long countOverdue(@Param("workspaceId") String workspaceId,
                      @Param("doneStatus") TaskStatus doneStatus,
                      @Param("now") Instant now);
}
