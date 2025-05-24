
package com.project.Trinity.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String token;

 @ManyToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @Column(nullable = false)
 private LocalDateTime expiryDate;

 public PasswordResetToken() {}

 public PasswordResetToken(String token, User user, LocalDateTime expiryDate) {
     this.token = token;
     this.user = user;
     this.expiryDate = expiryDate;
 }

 public boolean isExpired() {
     return LocalDateTime.now().isAfter(expiryDate);
 }
}