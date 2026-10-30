package com.mycompany.projectmanagementapp.service;

import com.mycompany.projectmanagementapp.dto.task.CommentResponse;
import com.mycompany.projectmanagementapp.dto.task.CreateCommentRequest;
import com.mycompany.projectmanagementapp.model.Comment;
import com.mycompany.projectmanagementapp.model.Task;
import com.mycompany.projectmanagementapp.model.User;
import com.mycompany.projectmanagementapp.exception.ApiException;
import com.mycompany.projectmanagementapp.repository.CommentRepository;
import com.mycompany.projectmanagementapp.repository.TaskRepository;
import com.mycompany.projectmanagementapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectAccess projectAccess;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          ProjectAccess projectAccess) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectAccess = projectAccess;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(String taskId, String userId) {
        Task task = getTask(taskId);
        projectAccess.requireView(task.getProject().getId(), userId);
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse add(String taskId, String userId, CreateCommentRequest request) {
        Task task = getTask(taskId);
        projectAccess.requireContribute(task.getProject().getId(), userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(author);
        comment.setContent(request.content().trim());
        return CommentResponse.from(commentRepository.saveAndFlush(comment));
    }

    /** The author can delete their own comment; the team lead / workspace ADMIN can delete any comment. */
    @Transactional
    public void delete(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> ApiException.notFound("Comment not found"));
        boolean author = comment.getUser().getId().equals(userId);
        if (!author && !projectAccess.canManage(comment.getTask().getProject(), userId)) {
            throw ApiException.forbidden("You cannot delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Task getTask(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> ApiException.notFound("Task not found"));
    }
}
