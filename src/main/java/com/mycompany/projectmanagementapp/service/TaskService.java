package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.task.CreateTaskRequest;
import com.mycompany.projectmanagementapp.dto.task.TaskResponse;
import com.mycompany.projectmanagementapp.dto.task.UpdateTaskRequest;
import com.mycompany.projectmanagementapp.model.Project;
import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.enums.Priority;
import com.mycompany.projectmanagementapp.enums.TaskStatus;
import com.mycompany.projectmanagementapp.enums.TaskType;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.TaskRepository;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import com.mycompany.projectmanagementapp.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectAccess projectAccess;
    private final WorkspaceAccess workspaceAccess;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ProjectAccess projectAccess,
                       WorkspaceAccess workspaceAccess) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectAccess = projectAccess;
        this.workspaceAccess = workspaceAccess;
    }

    @Transactional
    public TaskResponse create(String projectId, String userId, CreateTaskRequest request) {
        Project project = projectAccess.requireContribute(projectId, userId);
        User assignee = resolveAssignee(project, request.assigneeId());

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.type() != null) {
            task.setType(request.type());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setAssignee(assignee);
        task.setDueDate(DateTimeUtil.parse(request.dueDate(), "due_date"));

        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(String projectId, String userId,
                                   TaskStatus status, TaskType type, Priority priority, String assigneeId) {
        projectAccess.requireView(projectId, userId);
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> assigneeId == null || assigneeId.isBlank() || t.getAssignee().getId().equals(assigneeId))
                .map(TaskResponse::from)
                .toList();
    }

    /** Tasks assigned to the currently logged-in user within a workspace (for the "My Tasks" sidebar). */
    @Transactional(readOnly = true)
    public List<TaskResponse> listMine(String workspaceId, String userId) {
        workspaceAccess.requireMember(workspaceId, userId);
        return taskRepository.findByProjectWorkspaceIdAndAssigneeId(workspaceId, userId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(String taskId, String userId) {
        Task task = getTask(taskId);
        projectAccess.requireView(task.getProject().getId(), userId);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(String taskId, String userId, UpdateTaskRequest request) {
        Task task = getTask(taskId);
        Project project = projectAccess.requireContribute(task.getProject().getId(), userId);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw ApiException.badRequest("Task title must not be blank");
            }
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.type() != null) {
            task.setType(request.type());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.assigneeId() != null && !request.assigneeId().isBlank()) {
            task.setAssignee(resolveAssignee(project, request.assigneeId()));
        }
        if (request.dueDate() != null && !request.dueDate().isBlank()) {
            task.setDueDate(DateTimeUtil.parse(request.dueDate(), "due_date"));
        }
        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse updateStatus(String taskId, String userId, TaskStatus status) {
        Task task = getTask(taskId);
        projectAccess.requireContribute(task.getProject().getId(), userId);
        task.setStatus(status);
        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public void delete(String taskId, String userId) {
        Task task = getTask(taskId);
        projectAccess.requireManage(task.getProject().getId(), userId);
        taskRepository.delete(task);
    }

    /** Delete multiple tasks (frontend supports selecting multiple tasks and deleting them). Requires management rights on all related projects. */
    @Transactional
    public void bulkDelete(String userId, List<String> ids) {
        List<Task> tasks = taskRepository.findByIdIn(ids);
        Set<String> foundIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
        for (String id : ids) {
            if (!foundIds.contains(id)) {
                throw ApiException.notFound("Task not found: " + id);
            }
        }

        Set<String> checkedProjects = new HashSet<>();
        for (Task task : tasks) {
            String projectId = task.getProject().getId();
            if (checkedProjects.add(projectId)) {
                projectAccess.requireManage(projectId, userId);
            }
        }
        taskRepository.deleteAll(tasks);
    }

    private Task getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> ApiException.notFound("Task not found"));
    }

    private User resolveAssignee(Project project, String assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> ApiException.notFound("Assignee not found"));
        if (!projectAccess.isParticipant(project, assignee.getId())) {
            throw ApiException.badRequest("Assignee must be a member of the project");
        }
        return assignee;
    }
}
