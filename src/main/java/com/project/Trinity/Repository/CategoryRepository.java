package com.project.Trinity.Repository;

import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByStatus(Status status);
}