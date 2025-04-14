package com.project.Trinity.DTO;

import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequest {
    @NotBlank(message = "Kullanıcı adı zorunludur")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}