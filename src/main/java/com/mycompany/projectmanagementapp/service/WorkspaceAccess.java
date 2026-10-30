package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.model.WorkspaceMember;
import com.mycompany.projectmanagementapp.enums.WorkspaceRole;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import com.mycompany.projectmanagementapp.repository.WorkspaceMemberRepository;
import com.mycompany.projectmanagementapp.repository.WorkspaceRepository;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Workspace-level permission checks shared across services. Call this inside @Transactional. */
@Component
public class WorkspaceAccess {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WorkspaceAccess(WorkspaceRepository workspaceRepository,
                           WorkspaceMemberRepository memberRepository,
                           UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    /** User must be a member of the workspace. */
    public WorkspaceMember requireMember(String workspaceId, String userId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw ApiException.notFound("Workspace not found");
        }
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> ApiException.forbidden("You are not a member of this workspace"));
    }

    /** User must be an ADMIN of the workspace. */
    public WorkspaceMember requireAdmin(String workspaceId, String userId) {
        WorkspaceMember member = requireMember(workspaceId, userId);
        if (member.getRole() != WorkspaceRole.ADMIN) {
            throw ApiException.forbidden("Only workspace admins can perform this action");
        }
        return member;
    }

    /** Find a user by email and require that the user already belongs to the workspace (used when assigning them to a project). */
    public User findWorkspaceUserByEmail(String workspaceId, String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> ApiException.notFound("No registered user with email " + normalized));
        if (!memberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            throw ApiException.badRequest(normalized + " is not a member of this workspace");
        }
        return user;
    }
}
