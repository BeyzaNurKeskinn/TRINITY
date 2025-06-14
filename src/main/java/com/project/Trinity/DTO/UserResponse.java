package com.project.Trinity.DTO;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String status;  // Yeni alan
    private String role;  

    public UserResponse(Long id, String username, String email, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }
    public UserResponse(Long id, String username, String email, String phone, String status, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.role = role;
    }
}