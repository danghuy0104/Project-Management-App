package com.mycompany.projectmanagementapp.repository;

import com.mycompany.projectmanagementapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    /** Email luôn được lưu dạng chữ thường. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
