package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.overview.ProjectOverviewResponse;
import com.mycompany.projectmanagementapp.dto.overview.WorkspaceOverviewResponse;
import com.mycompany.projectmanagementapp.dto.overview.WorkspaceStatsResponse;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceMemberResponse;
import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.model.ProjectMember;
import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.enums.ProjectStatus;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.repository.ProjectMemberRepository;
import com.mycompany.projectmanagementapp.repository.ProjectRepository;
import com.mycompany.projectmanagementapp.repository.TaskRepository;
import com.mycompany.projectmanagementapp.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OverviewService {

    private final WorkspaceMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceAccess workspaceAccess;

    public OverviewService(WorkspaceMemberRepository memberRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           TaskRepository taskRepository,
                           WorkspaceAccess workspaceAccess) {
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.workspaceAccess = workspaceAccess;
    }

    @Transactional(readOnly = true)
    public WorkspaceOverviewResponse overview(String workspaceId, String userId) {
        WorkspaceMember me = workspaceAccess.requireMember(workspaceId, userId);

        List<WorkspaceMemberResponse> members = memberRepository.findByWorkspaceId(workspaceId).stream()
                .map(WorkspaceMemberResponse::from)
                .toList();

        Map<String, List<ProjectMember>> membersByProject = projectMemberRepository
                .findByProjectWorkspaceId(workspaceId).stream()
                .collect(Collectors.groupingBy(m -> m.getProject().getId()));
        Map<String, List<Task>> tasksByProject = taskRepository
                .findByProjectWorkspaceId(workspaceId).stream()
                .collect(Collectors.groupingBy(t -> t.getProject().getId()));

        List<Project> projects = projectRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
        List<ProjectOverviewResponse> projectResponses = projects.stream()
                .map(p -> ProjectOverviewResponse.from(p, membersByProject.get(p.getId()), tasksByProject.get(p.getId())))
                .toList();

        return WorkspaceOverviewResponse.from(me.getWorkspace(), me.getRole(), members, projectResponses);
    }

    @Transactional(readOnly = true)
    public WorkspaceStatsResponse stats(String workspaceId, String userId) {
        workspaceAccess.requireMember(workspaceId, userId);

        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByProjectWorkspaceIdAndStatus(workspaceId, status));
        }

        return new WorkspaceStatsResponse(
                projectRepository.countByWorkspaceId(workspaceId),
                projectRepository.countByWorkspaceIdAndStatus(workspaceId, ProjectStatus.ACTIVE),
                projectRepository.countByWorkspaceIdAndStatus(workspaceId, ProjectStatus.COMPLETED),
                taskRepository.countByProjectWorkspaceId(workspaceId),
                taskRepository.countByProjectWorkspaceIdAndAssigneeId(workspaceId, userId),
                taskRepository.countOverdue(workspaceId, TaskStatus.DONE, Instant.now()),
                tasksByStatus);
    }
}
