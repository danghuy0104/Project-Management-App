package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.ProjectMemberRepository;
import com.mycompany.projectmanagementapp.repository.ProjectRepository;
import org.springframework.stereotype.Component;

/**
 * Project-level authorization rules (called inside @Transactional):
 *  - view: all workspace members
 *  - contribute (create/edit tasks, comments): team lead, project member, or workspace ADMIN
 *  - manage (edit/delete project, manage members, delete tasks): team lead or workspace ADMIN
 */
@Component
public class ProjectAccess {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceAccess workspaceAccess;

    public ProjectAccess(ProjectRepository projectRepository,
                         ProjectMemberRepository projectMemberRepository,
                         WorkspaceAccess workspaceAccess) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceAccess = workspaceAccess;
    }

    public Project getProject(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> ApiException.notFound("Project not found"));
    }

    public Project requireView(String projectId, String userId) {
        Project project = getProject(projectId);
        workspaceAccess.requireMember(project.getWorkspace().getId(), userId);
        return project;
    }

    public Project requireContribute(String projectId, String userId) {
        Project project = getProject(projectId);
        WorkspaceMember member = workspaceAccess.requireMember(project.getWorkspace().getId(), userId);
        if (member.getRole() != WorkspaceRole.ADMIN && !isParticipant(project, userId)) {
            throw ApiException.forbidden("You are not a member of this project");
        }
        return project;
    }

    public Project requireManage(String projectId, String userId) {
        Project project = getProject(projectId);
        if (!canManage(project, userId)) {
            throw ApiException.forbidden("Only the team lead or a workspace admin can do this");
        }
        return project;
    }

    public boolean canManage(Project project, String userId) {
        WorkspaceMember member = workspaceAccess.requireMember(project.getWorkspace().getId(), userId);
        return member.getRole() == WorkspaceRole.ADMIN || isTeamLead(project, userId);
    }

    /** Team lead or project member (excluding workspace ADMIN privileges). */
    public boolean isParticipant(Project project, String userId) {
        return isTeamLead(project, userId)
                || projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId);
    }

    private boolean isTeamLead(Project project, String userId) {
        return project.getOwner().getId().equals(userId);
    }
}
