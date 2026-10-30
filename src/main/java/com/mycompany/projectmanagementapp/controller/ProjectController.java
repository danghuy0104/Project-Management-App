package com.mycompany.projectmanagementapp.controller;

import com.mycompany.projectmanagementapp.dto.project.AddProjectMemberRequest;
import com.mycompany.projectmanagementapp.dto.project.CreateProjectRequest;
import com.mycompany.projectmanagementapp.dto.project.ProjectMemberResponse;
import com.mycompany.projectmanagementapp.dto.project.ProjectResponse;
import com.mycompany.projectmanagementapp.dto.project.UpdateProjectRequest;
import com.mycompany.projectmanagementapp.security.UserPrincipal;
import com.mycompany.projectmanagementapp.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/workspaces/{workspaceId}/projects")
    public ResponseEntity<ProjectResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable("workspaceId") String workspaceId,
                                                  @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(workspaceId, principal.getId(), request));
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    public List<ProjectResponse> listByWorkspace(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable("workspaceId") String workspaceId) {
        return projectService.listByWorkspace(workspaceId, principal.getId());
    }

    @GetMapping("/projects/{projectId}")
    public ProjectResponse get(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable("projectId") String projectId) {
        return projectService.get(projectId, principal.getId());
    }

    @RequestMapping(value = "/projects/{projectId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ProjectResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable("projectId") String projectId,
                                  @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(projectId, principal.getId(), request);
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable("projectId") String projectId) {
        projectService.delete(projectId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}/members")
    public List<ProjectMemberResponse> listMembers(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable("projectId") String projectId) {
        return projectService.listMembers(projectId, principal.getId());
    }

    @PostMapping("/projects/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse> addMember(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable("projectId") String projectId,
                                                           @Valid @RequestBody AddProjectMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addMember(projectId, principal.getId(), request));
    }

    @DeleteMapping("/projects/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("projectId") String projectId,
                                             @PathVariable("memberId") String memberId) {
        projectService.removeMember(projectId, principal.getId(), memberId);
        return ResponseEntity.noContent().build();
    }
}
