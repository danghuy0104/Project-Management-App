package com.mycompany.projectmanagementapp.controller;

import com.mycompany.projectmanagementapp.dto.workspace.AddWorkspaceMemberRequest;
import com.mycompany.projectmanagementapp.dto.workspace.CreateWorkspaceRequest;
import com.mycompany.projectmanagementapp.dto.workspace.UpdateMemberRoleRequest;
import com.mycompany.projectmanagementapp.dto.workspace.UpdateWorkspaceRequest;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceMemberResponse;
import com.mycompany.projectmanagementapp.dto.workspace.WorkspaceResponse;
import com.mycompany.projectmanagementapp.security.UserPrincipal;
import com.mycompany.projectmanagementapp.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody CreateWorkspaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(principal.getId(), request));
    }

    @GetMapping
    public List<WorkspaceResponse> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return workspaceService.listMine(principal.getId());
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse get(@AuthenticationPrincipal UserPrincipal principal,
                                 @PathVariable("workspaceId") String workspaceId) {
        return workspaceService.get(workspaceId, principal.getId());
    }

    @PutMapping("/{workspaceId}")
    public WorkspaceResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable("workspaceId") String workspaceId,
                                    @Valid @RequestBody UpdateWorkspaceRequest request) {
        return workspaceService.update(workspaceId, principal.getId(), request);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable("workspaceId") String workspaceId) {
        workspaceService.delete(workspaceId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}/members")
    public List<WorkspaceMemberResponse> listMembers(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable("workspaceId") String workspaceId) {
        return workspaceService.listMembers(workspaceId, principal.getId());
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<WorkspaceMemberResponse> addMember(@AuthenticationPrincipal UserPrincipal principal,
                                                             @PathVariable("workspaceId") String workspaceId,
                                                             @Valid @RequestBody AddWorkspaceMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.addMember(workspaceId, principal.getId(), request));
    }

    @PatchMapping("/{workspaceId}/members/{memberId}")
    public WorkspaceMemberResponse updateMemberRole(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable("workspaceId") String workspaceId,
                                                    @PathVariable("memberId") String memberId,
                                                    @Valid @RequestBody UpdateMemberRoleRequest request) {
        return workspaceService.updateMemberRole(workspaceId, principal.getId(), memberId, request);
    }

    @DeleteMapping("/{workspaceId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("workspaceId") String workspaceId,
                                             @PathVariable("memberId") String memberId) {
        workspaceService.removeMember(workspaceId, principal.getId(), memberId);
        return ResponseEntity.noContent().build();
    }
}
