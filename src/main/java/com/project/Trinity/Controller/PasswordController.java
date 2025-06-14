package com.project.Trinity.Controller;

import com.project.Trinity.Entity.Password;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Service.PasswordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/passwords")
    public ResponseEntity<PasswordResponse> savePassword(@Valid @RequestBody PasswordRequest request) {
        Password password = passwordService.savePassword(
                request.getId(),
                request.getCategoryId(),
                request.getTitle(),
                request.getUsername(),
                request.getPassword(),
                request.getStatus(),
                request.getDescription()
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

    @GetMapping("/passwords/by-category")
    public ResponseEntity<List<PasswordResponse>> getPasswordsByCategory(@RequestParam String category) {
        List<PasswordResponse> passwords = passwordService.getPasswordsByCategory(category)
                .stream()
                .map(PasswordResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(passwords);
    }
    @PutMapping("/passwords/{id}/toggle-featured")
    public ResponseEntity<PasswordResponse> toggleFeatured(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isFeatured = request.get("isFeatured");
        Password password = passwordService.toggleFeatured(id, isFeatured);
        if (!password.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bu şifre size ait değil.");
        }
        return ResponseEntity.ok(new PasswordResponse(password));
    }
    @GetMapping("/featured-passwords")
    public ResponseEntity<List<PasswordResponse>> getFeaturedPasswords() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<PasswordResponse> featuredPasswords = passwordService.getFeaturedPasswordsByUser(currentUser)
                .stream()
                .map(PasswordResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(featuredPasswords);
    }
    @GetMapping("/most-viewed-passwords")
    public ResponseEntity<List<PasswordResponse>> getMostViewedPasswords() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<PasswordResponse> mostViewedPasswords = passwordService.getMostViewedPasswordsByUser(currentUser, 5)
                .stream()
                .map(PasswordResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mostViewedPasswords);
    }

    @PutMapping("/passwords/{id}")
    public ResponseEntity<PasswordResponse> updatePassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        Password password = passwordService.updatePassword(
            id,
            request.getCategoryId(),
            request.getTitle(),
            request.getUsername(),
            request.getPassword(),
            request.getStatus(),
            request.getDescription()
        );
        return ResponseEntity.ok(new PasswordResponse(password));
    }
    
    @DeleteMapping("/passwords/{id}")
    public ResponseEntity<Void> deletePassword(@PathVariable Long id) {
        passwordService.deletePassword(id);
        return ResponseEntity.noContent().build();
    }
}
@Data
class ToggleFeaturedRequest {
    private boolean isFeatured;
}

@Data
class PasswordUpdateRequest extends PasswordRequest {
    @Pattern(regexp = "^[0-9]{6}$", message = "Doğrulama kodu 6 haneli olmalı")
    private String verificationCode; // Doğrulama kodu eklendi
}
@Data
class PasswordRequest {
    private Long id;
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
    @jakarta.validation.constraints.Size(max = 500, message = "Açıklama 500 karakterden uzun olamaz")
    private String description;
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status sadece ACTIVE veya INACTIVE olabilir")
    private String status = "ACTIVE";
}

@Data
class PasswordResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String username;
    private String description;
    private String status;
    private boolean isFeatured;

    public PasswordResponse(Password password) {
        this.id = password.getId();
        this.categoryId = password.getCategory().getId();
        this.categoryName = password.getCategory().getName();
        this.title = password.getTitle();
        this.username = password.getUsername();
        this.description = password.getDescription();
        this.status = password.getStatus().getDisplayName();
        this.isFeatured = password.getIsFeatured(); 
    }
}