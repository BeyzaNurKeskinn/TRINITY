package com.project.Trinity.Repository;
import com.project.Trinity.Entity.Password;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordRepository extends JpaRepository<Password, Long> {
    List<Password> findByCreatedByAndStatus(User createdBy, Status status);
}