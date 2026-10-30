package com.mycompany.projectmanagementapp.controller;

import com.mycompany.projectmanagementapp.dto.overview.WorkspaceOverviewResponse;
import com.mycompany.projectmanagementapp.dto.overview.WorkspaceStatsResponse;
import com.mycompany.projectmanagementapp.security.UserPrincipal;
import com.mycompany.projectmanagementapp.service.OverviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/overview")
    public WorkspaceOverviewResponse overview(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable("workspaceId") String workspaceId) {
        return overviewService.overview(workspaceId, principal.getId());
    }

    @GetMapping("/stats")
    public WorkspaceStatsResponse stats(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable("workspaceId") String workspaceId) {
        return overviewService.stats(workspaceId, principal.getId());
    }
}
