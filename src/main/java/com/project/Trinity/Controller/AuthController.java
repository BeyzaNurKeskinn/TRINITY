
package com.project.Trinity.Controller;

import com.project.Trinity.Service.RefreshTokenService;
import com.project.Trinity.Entity.PasswordResetToken;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.PasswordResetTokenRepository;
import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Service.EmailService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/auth")
public class AuthController {//Kullanıcı kaydı, girişi ve refresh token işlemlerini yöneten REST endpoint’leri.
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager; // Yeni bağımlılık
    private final JwtUtil jwtUtil; // Yeni bağımlılık
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                          EmailService emailService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
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
            return ResponseEntity.ok("Sıfırlama kodu e-postanıza veya telefonunuza gönderildi.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Bu e-posta veya telefon numarası kayıtlı değil. Destek için support@trinity.com ile iletişime geçin.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("E-posta gönderimi başarısız: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Şifre başarıyla sıfırlandı.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/user/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
            String code = String.format("%06d", new Random().nextInt(999999));
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);
            PasswordResetToken token = new PasswordResetToken(code, user, expiryDate);
            tokenRepository.save(token);
            emailService.sendResetCodeEmail(user.getEmail(), code);
            return ResponseEntity.ok("Doğrulama kodu gönderildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Doğrulama kodu gönderimi başarısız: " + e.getMessage());
        }
    }

    @PostMapping("/user/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(request.getCode())
                    .orElseThrow(() -> new IllegalArgumentException("Geçersiz doğrulama kodu"));
            if (resetToken.isExpired()) {
                throw new IllegalArgumentException("Doğrulama kodu süresi dolmuş");
            }
            tokenRepository.delete(resetToken);
            return ResponseEntity.ok("Doğrulama başarılı.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
class VerifyCodeRequest {
    @NotBlank(message = "Doğrulama kodu zorunludur")
    private String code;
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
class ResetPasswordRequest {
    @NotBlank(message = "Sıfırlama kodu zorunludur")
    private String token;

    @NotBlank(message = "Yeni şifre zorunludur")
    @Size(min = 8, message = "Yeni şifre en az 8 karakter olmalı")
    private String newPassword;
}
@Data
class AuthenticationRequest {
    @NotBlank(message = "Kullanıcı adı zorunludur")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    private String password;
}