
package com.project.Trinity.Controller;

import com.project.Trinity.Service.RefreshTokenService;

import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Service.InvalidRefreshTokenException;
import com.project.Trinity.Service.UserService;
import com.project.Trinity.Service.UsernameAlreadyExistsException;
import com.project.Trinity.Util.JwtUtil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/auth")
public class AuthController {//Kullanıcı kaydı, girişi ve refresh token işlemlerini yöneten REST endpoint’leri.
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager; // Yeni bağımlılık
    private final JwtUtil jwtUtil; // Yeni bağımlılık
    private final UserRepository userRepository;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,UserRepository userRepository) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank() ||
            request.getEmail() == null || request.getEmail().isBlank() ||
            request.getPhone() == null || request.getPhone().isBlank()) {
            return new ResponseEntity<>("Tüm alanlar zorunludur", HttpStatus.BAD_REQUEST);
        }
        try {
            userService.createUser(request.getUsername(), request.getPassword(), request.getEmail(), request.getPhone());
            return new ResponseEntity<>("Kullanıcı başarıyla kaydedildi", HttpStatus.CREATED);
        } catch (UsernameAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Kayıt başarısız: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthenticationRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(Map.of("error", "Kimlik doğrulama başarısız"), HttpStatus.UNAUTHORIZED);
        }
    }
  

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return new ResponseEntity<>("Yenileme token'ı zorunludur", HttpStatus.BAD_REQUEST);
        }
        try {
            String newAccessToken = refreshTokenService.generateNewAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (InvalidRefreshTokenException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.sendResetLink(request.getEmailOrPhone());
            return ResponseEntity.ok("Reset link sent to your email or phone.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No account found with this email or phone. Contact support at support@trinity.com");
        }
    }
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String credential = request.get("credential");
        // Google token doğrulama ve kullanıcı oluşturma mantığı
        System.out.println("Google login with credential: " + credential);
        return ResponseEntity.ok("Google login success");
    }

    @PostMapping("/facebook-login")
    public ResponseEntity<?> facebookLogin(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        // Facebook token doğrulama ve kullanıcı oluşturma mantığı
        System.out.println("Facebook login with accessToken: " + accessToken);
        return ResponseEntity.ok("Facebook login success");
    }
}
//AuthController, kullanıcıyla ilgili temel işlemleri (kayıt, giriş, token yenileme) yönetir. REST API’nin yüzü gibidir.
// DTO Classes
@Data
class RegisterRequest {
	@NotBlank(message = "Kullanıcı adı zorunludur")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3-20 karakter olmalı")
    private String username;
	
	@NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalı")
    private String password;
	
	
    @Size(min = 5, max = 255, message = "Email 5-255 karakter olmalı")
    private String email;

   
    @Size(min = 10, max = 15, message = "Telefon numarası 10-15 karakter olmalı")
    private String phone;

}
@Data
class ForgotPasswordRequest {
    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;
}
class RefreshTokenRequest {
	@NotBlank(message = "Yenileme token'ı zorunludur")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
@Data
class AuthenticationRequest {
    @NotBlank(message = "Kullanıcı adı zorunludur")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    private String password;
}