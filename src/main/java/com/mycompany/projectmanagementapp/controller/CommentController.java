package com.mycompany.projectmanagementapp.controller;

import com.mycompany.projectmanagementapp.dto.task.CommentResponse;
import com.mycompany.projectmanagementapp.dto.task.CreateCommentRequest;
import com.mycompany.projectmanagementapp.security.UserPrincipal;
import com.mycompany.projectmanagementapp.service.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable("taskId") String taskId) {
        return commentService.list(taskId, principal.getId());
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable("taskId") String taskId,
                                               @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.add(taskId, principal.getId(), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable("commentId") String commentId) {
        commentService.delete(commentId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
