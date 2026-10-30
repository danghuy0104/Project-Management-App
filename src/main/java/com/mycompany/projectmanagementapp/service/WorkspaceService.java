package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.workspace.AddWorkspaceMemberRequest;
import com.mycompany.projectmanagementapp.dto.workspace.CreateWorkspaceRequest;
import com.mycompany.projectmanagementapp.dto.workspace.UpdateMemberRoleRequest;
import com.mycompany.projectmanagementapp.dto.workspace.UpdateWorkspaceRequest;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceMemberResponse;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceResponse;
import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.model.Workspace;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.ProjectMemberRepository;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import com.mycompany.projectmanagementapp.repository.WorkspaceMemberRepository;
import com.mycompany.projectmanagementapp.repository.WorkspaceRepository;
import com.mycompany.projectmanagementapp.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceAccess workspaceAccess;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository memberRepository,
                            ProjectMemberRepository projectMemberRepository,
                            UserRepository userRepository,
                            WorkspaceAccess workspaceAccess) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.workspaceAccess = workspaceAccess;
    }

    /** The creator becomes the owner and the first ADMIN of the workspace. */
    @Transactional
    public WorkspaceResponse create(String userId, CreateWorkspaceRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));

        String slugSource = (request.slug() != null && !request.slug().isBlank()) ? request.slug() : request.name();

        Workspace workspace = new Workspace();
        workspace.setName(request.name().trim());
        workspace.setDescription(request.description());
        workspace.setImageUrl(request.imageUrl() == null ? "" : request.imageUrl());
        workspace.setSlug(generateUniqueSlug(slugSource));
        workspace.setOwner(owner);
        workspace = workspaceRepository.saveAndFlush(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setUser(owner);
        member.setWorkspace(workspace);
        member.setRole(WorkspaceRole.ADMIN);
        memberRepository.save(member);

        return WorkspaceResponse.from(workspace, WorkspaceRole.ADMIN);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listMine(String userId) {
        return memberRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing((WorkspaceMember m) -> m.getWorkspace().getCreatedAt()))
                .map(m -> WorkspaceResponse.from(m.getWorkspace(), m.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse get(String workspaceId, String userId) {
        WorkspaceMember me = workspaceAccess.requireMember(workspaceId, userId);
        return WorkspaceResponse.from(me.getWorkspace(), me.getRole());
    }

    @Transactional
    public WorkspaceResponse update(String workspaceId, String userId, UpdateWorkspaceRequest request) {
        WorkspaceMember me = workspaceAccess.requireAdmin(workspaceId, userId);
        Workspace workspace = me.getWorkspace();

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw ApiException.badRequest("Workspace name must not be blank");
            }
            workspace.setName(request.name().trim());
        }
        if (request.description() != null) {
            workspace.setDescription(request.description());
        }
        if (request.imageUrl() != null) {
            workspace.setImageUrl(request.imageUrl());
        }
        workspace = workspaceRepository.saveAndFlush(workspace);
        return WorkspaceResponse.from(workspace, me.getRole());
    }

    /** Only the owner can delete it. Child data (members, projects, tasks, comments) is deleted via ON DELETE CASCADE. */
    @Transactional
    public void delete(String workspaceId, String userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> ApiException.notFound("Workspace not found"));
        if (!workspace.getOwner().getId().equals(userId)) {
            throw ApiException.forbidden("Only the workspace owner can delete it");
        }
        workspaceRepository.delete(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> listMembers(String workspaceId, String userId) {
        workspaceAccess.requireMember(workspaceId, userId);
        return memberRepository.findByWorkspaceId(workspaceId).stream()
                .map(WorkspaceMemberResponse::from)
                .toList();
    }

    /** Invite by email: the invited user must already have a registered account. */
    @Transactional
    public WorkspaceMemberResponse addMember(String workspaceId, String userId, AddWorkspaceMemberRequest request) {
        WorkspaceMember me = workspaceAccess.requireAdmin(workspaceId, userId);

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User invited = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("No registered user with email " + email));
        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, invited.getId())) {
            throw ApiException.conflict("User is already a member of this workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setUser(invited);
        member.setWorkspace(me.getWorkspace());
        member.setRole(WorkspaceRole.fromString(request.role()));
        member.setMessage(request.message() == null ? "" : request.message());
        return WorkspaceMemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public WorkspaceMemberResponse updateMemberRole(String workspaceId, String userId, String memberId,
                                                    UpdateMemberRoleRequest request) {
        WorkspaceMember me = workspaceAccess.requireAdmin(workspaceId, userId);
        WorkspaceMember target = memberRepository.findByIdAndWorkspaceId(memberId, workspaceId)
                .orElseThrow(() -> ApiException.notFound("Member not found"));

        if (target.getUser().getId().equals(me.getWorkspace().getOwner().getId())) {
            throw ApiException.badRequest("The role of the workspace owner cannot be changed");
        }
        target.setRole(WorkspaceRole.fromString(request.role()));
        return WorkspaceMemberResponse.from(memberRepository.saveAndFlush(target));
    }

    /** Admins can remove others; regular members can only leave on their own. The owner cannot be removed. */
    @Transactional
    public void removeMember(String workspaceId, String userId, String memberId) {
        WorkspaceMember me = workspaceAccess.requireMember(workspaceId, userId);
        WorkspaceMember target = memberRepository.findByIdAndWorkspaceId(memberId, workspaceId)
                .orElseThrow(() -> ApiException.notFound("Member not found"));

        boolean removingSelf = target.getUser().getId().equals(userId);
        if (!removingSelf && me.getRole() != WorkspaceRole.ADMIN) {
            throw ApiException.forbidden("Only workspace admins can remove other members");
        }
        if (target.getUser().getId().equals(me.getWorkspace().getOwner().getId())) {
            throw ApiException.badRequest("The workspace owner cannot be removed");
        }

        // Remove this user from all projects in the workspace before removing them from the workspace.
        projectMemberRepository.deleteByProjectWorkspaceIdAndUserId(workspaceId, target.getUser().getId());
        memberRepository.delete(target);
    }

    private String generateUniqueSlug(String source) {
        String base = SlugUtil.slugify(source);
        if (base.isBlank()) {
            base = "workspace";
        }
        String slug = base;
        while (workspaceRepository.existsBySlug(slug)) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }
}
