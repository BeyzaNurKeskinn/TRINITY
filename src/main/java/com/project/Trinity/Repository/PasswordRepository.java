package com.project.Trinity.Repository;

import com.project.Trinity.Entity.Password;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PasswordRepository extends JpaRepository<Password, Long> {
    List<Password> findByCreatedByAndStatus(User createdBy, Status status);

    List<Password> findByUserAndCategoryName(User user, String categoryName);

    List<Password> findByUser(User user);

    @Query("SELECT DISTINCT p.category.name FROM Password p WHERE p.user = :user AND p.status = 'ACTIVE'")
    List<String> findDistinctCategoryByUser(@Param("user") User user);
}