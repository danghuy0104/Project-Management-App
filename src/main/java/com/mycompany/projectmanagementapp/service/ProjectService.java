package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.project.AddProjectMemberRequest;
import com.mycompany.projectmanagementapp.dto.project.CreateProjectRequest;
import com.mycompany.projectmanagementapp.dto.project.ProjectMemberResponse;
import com.mycompany.projectmanagementapp.dto.project.ProjectResponse;
import com.mycompany.projectmanagementapp.dto.project.UpdateProjectRequest;
import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.model.ProjectMember;
import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.ProjectMemberRepository;
import com.mycompany.projectmanagementapp.repository.ProjectRepository;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import com.mycompany.projectmanagementapp.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccess workspaceAccess;
    private final ProjectAccess projectAccess;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository,
                          WorkspaceAccess workspaceAccess,
                          ProjectAccess projectAccess) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.workspaceAccess = workspaceAccess;
        this.projectAccess = projectAccess;
    }

    /** Any workspace member can create a project; the team lead is always added as a project member. */
    @Transactional
    public ProjectResponse create(String workspaceId, String userId, CreateProjectRequest request) {
        WorkspaceMember requester = workspaceAccess.requireMember(workspaceId, userId);

        User teamLead;
        if (request.teamLead() == null || request.teamLead().isBlank()) {
            teamLead = userRepository.findById(userId)
                    .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));
        } else {
            teamLead = workspaceAccess.findWorkspaceUserByEmail(workspaceId, request.teamLead());
        }

        Instant start = DateTimeUtil.parse(request.startDate(), "start_date");
        Instant end = DateTimeUtil.parse(request.endDate(), "end_date");
        validateDates(start, end);

        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.priority() != null) {
            project.setPriority(request.priority());
        }
        project.setStartDate(start);
        project.setEndDate(end);
        project.setOwner(teamLead);
        project.setWorkspace(requester.getWorkspace());
        project = projectRepository.saveAndFlush(project);

        Set<String> addedUserIds = new HashSet<>();
        List<ProjectMember> members = new ArrayList<>();
        members.add(newMember(project, teamLead));
        addedUserIds.add(teamLead.getId());

        if (request.teamMembers() != null) {
            for (String email : request.teamMembers()) {
                if (email == null || email.isBlank()) {
                    continue;
                }
                User user = workspaceAccess.findWorkspaceUserByEmail(workspaceId, email);
                if (addedUserIds.add(user.getId())) {
                    members.add(newMember(project, user));
                }
            }
        }
        members = projectMemberRepository.saveAll(members);

        return ProjectResponse.from(project, members);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listByWorkspace(String workspaceId, String userId) {
        workspaceAccess.requireMember(workspaceId, userId);

        List<Project> projects = projectRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
        Map<String, List<ProjectMember>> membersByProject = projectMemberRepository
                .findByProjectWorkspaceId(workspaceId).stream()
                .collect(Collectors.groupingBy(m -> m.getProject().getId()));

        return projects.stream()
                .map(p -> ProjectResponse.from(p, membersByProject.get(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(String projectId, String userId) {
        Project project = projectAccess.requireView(projectId, userId);
        return ProjectResponse.from(project, projectMemberRepository.findByProjectId(projectId));
    }

    @Transactional
    public ProjectResponse update(String projectId, String userId, UpdateProjectRequest request) {
        Project project = projectAccess.requireManage(projectId, userId);

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw ApiException.badRequest("Project name must not be blank");
            }
            project.setName(request.name().trim());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.priority() != null) {
            project.setPriority(request.priority());
        }
        if (request.startDate() != null) {
            project.setStartDate(DateTimeUtil.parse(request.startDate(), "start_date"));
        }
        if (request.endDate() != null) {
            project.setEndDate(DateTimeUtil.parse(request.endDate(), "end_date"));
        }
        if (request.progress() != null) {
            project.setProgress(request.progress());
        }
        validateDates(project.getStartDate(), project.getEndDate());

        project = projectRepository.saveAndFlush(project);
        return ProjectResponse.from(project, projectMemberRepository.findByProjectId(projectId));
    }

    /** Tasks and project members are deleted via ON DELETE CASCADE. */
    @Transactional
    public void delete(String projectId, String userId) {
        Project project = projectAccess.requireManage(projectId, userId);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(String projectId, String userId) {
        projectAccess.requireView(projectId, userId);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    /** Add by email (matching the AddProjectMember form); the user must belong to the project's workspace. */
    @Transactional
    public ProjectMemberResponse addMember(String projectId, String userId, AddProjectMemberRequest request) {
        Project project = projectAccess.requireManage(projectId, userId);
        User user = workspaceAccess.findWorkspaceUserByEmail(project.getWorkspace().getId(), request.email());

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw ApiException.conflict("User is already a member of this project");
        }
        return ProjectMemberResponse.from(projectMemberRepository.save(newMember(project, user)));
    }

    @Transactional
    public void removeMember(String projectId, String userId, String memberId) {
        Project project = projectAccess.requireManage(projectId, userId);
        ProjectMember member = projectMemberRepository.findByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> ApiException.notFound("Project member not found"));

        if (member.getUser().getId().equals(project.getOwner().getId())) {
            throw ApiException.badRequest("The team lead cannot be removed from the project");
        }
        projectMemberRepository.delete(member);
    }

    private ProjectMember newMember(Project project, User user) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        return member;
    }

    private void validateDates(Instant start, Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw ApiException.badRequest("end_date must not be before start_date");
        }
    }
}
