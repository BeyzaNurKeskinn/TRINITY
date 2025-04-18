package com.project.Trinity.Controller;

import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Password;
import com.project.Trinity.Service.CategoryService;
import com.project.Trinity.Service.PasswordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class PasswordController {

    private final PasswordService passwordService;
    private final CategoryService categoryService;

    public PasswordController(PasswordService passwordService, CategoryService categoryService) {
        this.passwordService = passwordService;
        this.categoryService = categoryService;
    }

    @PostMapping("/passwords")
    public ResponseEntity<PasswordResponse> savePassword(@Valid @RequestBody PasswordRequest request) {
        Password password = passwordService.savePassword(
                request.getId(),
                request.getCategoryId(),
                request.getTitle(),
                request.getUsername(),
                request.getPassword(),
                request.getStatus()
        );
        return new ResponseEntity<>(new PasswordResponse(password), HttpStatus.OK);
    }

    @GetMapping("/passwords")
    public ResponseEntity<List<PasswordResponse>> getUserPasswords() {
        List<PasswordResponse> passwords = passwordService.getUserPasswords()
                .stream()
                .map(PasswordResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(passwords);
    }

    @DeleteMapping("/passwords/{id}")
    public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
        passwordService.deletePassword(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category")
    public ResponseEntity<List<Category>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }
}

@Data
class PasswordRequest {
    private Long id; // Opsiyonel, güncelleme için

    @jakarta.validation.constraints.NotNull(message = "Kategori ID zorunludur")
    private Long categoryId;

    @jakarta.validation.constraints.NotBlank(message = "Başlık zorunludur")
    @jakarta.validation.constraints.Size(min = 3, max = 200, message = "Başlık 3-200 karakter olmalı")
    private String title;

    @jakarta.validation.constraints.NotBlank(message = "Kullanıcı girişi zorunludur")
    @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Kullanıcı girişi 3-100 karakter olmalı")
    private String username;

    @jakarta.validation.constraints.NotBlank(message = "Şifre zorunludur")
    @jakarta.validation.constraints.Size(min = 6, max = 100, message = "Şifre 6-100 karakter olmalı")
    private String password;

    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status sadece ACTIVE veya INACTIVE olabilir")
    private String status = "ACTIVE";
}

@Data
class PasswordResponse {
    private Long id;
    private Long categoryId;
    private String title;
    private String username;
    private String status;

    public PasswordResponse(Password password) {
        this.id = password.getId();
        this.categoryId = password.getCategory().getId();
        this.title = password.getTitle();
        this.username = password.getUsername();
        this.status = password.getStatus().getDisplayName();
    }
}