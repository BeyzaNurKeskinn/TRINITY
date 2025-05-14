package com.project.Trinity.Repository;

import com.project.Trinity.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
}//Kullanıcılar için veritabanı işlemlerini sağlar.
//Neden?: Kimlik doğrulama için kullanıcıyı bulur.