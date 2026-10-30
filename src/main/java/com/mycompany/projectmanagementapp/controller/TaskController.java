package com.mycompany.projectmanagementapp.controller;

import com.mycompany.projectmanagementapp.dto.task.BulkDeleteTasksRequest;
import com.mycompany.projectmanagementapp.dto.task.CreateTaskRequest;
import com.mycompany.projectmanagementapp.dto.task.TaskResponse;
import com.mycompany.projectmanagementapp.dto.task.UpdateTaskRequest;
import com.mycompany.projectmanagementapp.dto.task.UpdateTaskStatusRequest;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.enums.TaskType;
import com.mycompany.projectmanagementapp.security.UserPrincipal;
import com.mycompany.projectmanagementapp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable("projectId") String projectId,
                                               @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(projectId, principal.getId(), request));
    }

    /** Lọc tuỳ chọn: ?status=TODO&type=BUG&priority=HIGH&assigneeId=... */
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable("projectId") String projectId,
                                   @RequestParam(name = "status", required = false) TaskStatus status,
                                   @RequestParam(name = "type", required = false) TaskType type,
                                   @RequestParam(name = "priority", required = false) Priority priority,
                                   @RequestParam(name = "assigneeId", required = false) String assigneeId) {
        return taskService.list(projectId, principal.getId(), status, type, priority, assigneeId);
    }

    @GetMapping("/workspaces/{workspaceId}/my-tasks")
    public List<TaskResponse> listMine(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable("workspaceId") String workspaceId) {
        return taskService.listMine(workspaceId, principal.getId());
    }

    @GetMapping("/tasks/{taskId}")
    public TaskResponse get(@AuthenticationPrincipal UserPrincipal principal,
                            @PathVariable("taskId") String taskId) {
        return taskService.get(taskId, principal.getId());
    }

    @RequestMapping(value = "/tasks/{taskId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public TaskResponse update(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable("taskId") String taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(taskId, principal.getId(), request);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public TaskResponse updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable("taskId") String taskId,
                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(taskId, principal.getId(), request.status());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable("taskId") String taskId) {
        taskService.delete(taskId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody BulkDeleteTasksRequest request) {
        taskService.bulkDelete(principal.getId(), request.ids());
        return ResponseEntity.noContent().build();
    }
}
