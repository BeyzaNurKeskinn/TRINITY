package com.project.Trinity.Controller;

import com.project.Trinity.Service.RefreshTokenService;
import com.project.Trinity.DTO.AuthenticationRequest;
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

    public AuthController(UserService userService, RefreshTokenService refreshTokenService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank()) {
            return new ResponseEntity<>("Kullanıcı adı ve şifre zorunludur", HttpStatus.BAD_REQUEST);
        }
        try {
            userService.createUser(request.getUsername(), request.getPassword());
            return new ResponseEntity<>("Kullanıcı başarıyla kaydedildi", HttpStatus.CREATED);
        } catch (UsernameAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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

}

class RefreshTokenRequest {
	@NotBlank(message = "Yenileme token'ı zorunludur")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}