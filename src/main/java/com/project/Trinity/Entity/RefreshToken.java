package com.project.Trinity.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "refresh_token")
public class RefreshToken {//Refresh token’ları veritabanında saklamak için varlık sınıfı.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private LocalDateTime expiryDate;

    public RefreshToken() {
    }

}/*
RefreshToken, veritabanında token’ları saklar.
Refresh token’lar, kullanıcı oturumunu uzun süre devam ettirmek için kullanılır. 
Veritabanında saklanması, güvenliği artırır.*/