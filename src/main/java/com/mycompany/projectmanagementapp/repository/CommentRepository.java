package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByTaskIdOrderByCreatedAtAsc(String taskId);
}
