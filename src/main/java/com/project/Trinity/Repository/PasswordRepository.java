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

    // Yeni metod: Kullanıcı ve kategori adına göre yalnızca ACTIVE şifreleri getir
    @Query("SELECT p FROM Password p WHERE p.user = :user AND p.category.name = :categoryName AND p.status = :status")
    List<Password> findByUserAndCategoryNameAndStatus(@Param("user") User user, @Param("categoryName") String categoryName, @Param("status") Status status);
    
    @Query("SELECT p FROM Password p WHERE p.user = :user AND p.status = :status ORDER BY p.viewCount DESC")
    List<Password> findByUserAndStatusOrderByViewCountDesc(@Param("user") User user, @Param("status") Status status);

    List<Password> findByUserAndIsFeaturedTrueAndStatus(@Param("user") User user, @Param("status") Status status);
    
    @Query("SELECT p.category.name, COUNT(p) FROM Password p WHERE p.status = 'ACTIVE' GROUP BY p.category.name")
    List<Object[]> findPasswordCountByCategory();
}