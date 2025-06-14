package com.project.Trinity.Controller;

import com.project.Trinity.Entity.User;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Service.UserService;
import com.project.Trinity.DTO.UserResponse;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("USER");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
        String profilePictureBase64 = user.getProfilePicture() != null 
                ? Base64.getEncoder().encodeToString(user.getProfilePicture()) 
                : null;
        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                role,
                user.getStatus() != null ? user.getStatus().toString() : "ACTIVE",
                profilePictureBase64
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfoResponse> userResponses = users.stream()
                .map(user -> new UserInfoResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().name(),
                        user.getStatus() != null ? user.getStatus().toString() : "ACTIVE",
                        user.getProfilePicture() != null ? Base64.getEncoder().encodeToString(user.getProfilePicture()) : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(
                id,
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPhone(),
                request.getStatus(),
                request.getRole() // Role parametresi eklendi
        );
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + id));
        String profilePictureBase64 = user.getProfilePicture() != null 
                ? Base64.getEncoder().encodeToString(user.getProfilePicture()) 
                : null;
        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                userResponse.getUsername(),
                userResponse.getEmail(),
                userResponse.getPhone(),
                user.getRole().name(),
                request.getStatus() != null ? request.getStatus() : user.getStatus().toString(),
                		profilePictureBase64
        );
        return ResponseEntity.ok(response);
    }
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> createUser(@Valid @RequestBody UserUpdateRequest request) {
        try {
            // Null kontrolleri
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Varsayılan değerleri ayarla
            String status = request.getStatus() != null ? request.getStatus() : "ACTIVE";
            String role = request.getRole() != null ? request.getRole() : "USER";

            UserResponse userResponse = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPhone(),
                status,
                role
            );

            // Yeni kullanıcıyı DB'den çek
            User user = userRepository.findByUsername(userResponse.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + userResponse.getUsername()));

            String profilePictureBase64 = user.getProfilePicture() != null
                ? Base64.getEncoder().encodeToString(user.getProfilePicture())
                : null;

            UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                userResponse.getUsername(),
                userResponse.getEmail(),
                userResponse.getPhone(),
                user.getRole().name(),
                user.getStatus().toString(),
                profilePictureBase64
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Hata durumunda detaylı mesaj döndür
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/user/upload-profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        String username = authentication.getName();
        userService.uploadProfilePicture(username, file.getBytes());
        return ResponseEntity.ok("Profil resmi başarıyla yüklendi.");
    }
 // Yeni Endpoint: Kullanıcı kendi bilgilerini günceller
    @PutMapping("/user/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        UserResponse userResponse = userService.updateUser(
                user.getId(),
                request.getUsername(),
                request.getPassword(), // Şifre isteğe bağlı
                request.getEmail(),
                request.getPhone(),
                user.getStatus().toString(), // Mevcut durumu koru
                user.getRole().name() // Mevcut rolü koru
        );

        String profilePictureBase64 = user.getProfilePicture() != null 
                ? Base64.getEncoder().encodeToString(user.getProfilePicture()) 
                : null;
        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                userResponse.getUsername(),
                userResponse.getEmail(),
                userResponse.getPhone(),
                user.getRole().name(),
                user.getStatus().toString(),
                profilePictureBase64
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/freeze-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> freezeAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.freezeAccount(username);
        return ResponseEntity.ok("Hesabınız donduruldu. 30 gün içinde tekrar aktif hale getirmezseniz hesabınız silinecek.");
    }



    @Data
    public static class UserInfoResponse {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String role;
        private String status;
        private String profilePicture;

        public UserInfoResponse(Long id, String username, String email, String phone, String role, String status,String profilePicture) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
            this.profilePicture = profilePicture;
        }
    }

    @Data
    static class UserUpdateRequest {
        @jakarta.validation.constraints.NotBlank(message = "Kullanıcı adı zorunludur")
        @jakarta.validation.constraints.Size(min = 3, max = 20, message = "Kullanıcı adı 3-20 karakter olmalı")
        private String username;

        @jakarta.validation.constraints.Size(min = 8, message = "Şifre en az 8 karakter olmalı")
        private String password;

        @jakarta.validation.constraints.NotBlank(message = "E-posta zorunludur")
        @jakarta.validation.constraints.Email(message = "Geçerli bir e-posta giriniz")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Telefon numarası zorunludur")
        @jakarta.validation.constraints.Size(min = 10, max = 15, message = "Telefon numarası 10-15 karakter olmalı")
        private String phone;

        @jakarta.validation.constraints.Pattern(regexp = "ACTIVE|INACTIVE", message = "Durum ACTIVE veya INACTIVE olmalı")
        private String status;

        @jakarta.validation.constraints.Pattern(regexp = "USER|ADMIN", message = "Rol USER veya ADMIN olmalı")
        private String role;
    }
}