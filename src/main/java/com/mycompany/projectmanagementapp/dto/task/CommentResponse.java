package com.mycompany.projectmanagementapp.dto.task;

import com.mycompany.projectmanagementapp.dto.user.UserResponse;
import com.mycompany.projectmanagementapp.model.Comment;

import java.time.Instant;

public record CommentResponse(
        String id,
        String content,
        String userId,
        String taskId,
        Instant createdAt,
        UserResponse user) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getTask().getId(),
                comment.getCreatedAt(),
                UserResponse.from(comment.getUser()));
    }
}
