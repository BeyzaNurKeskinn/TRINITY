package com.project.Trinity.Service;

import com.project.Trinity.Entity.PasswordResetToken;
import com.project.Trinity.Entity.Role;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.PasswordResetTokenRepository;
import com.project.Trinity.Repository.RefreshTokenRepository;
import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.DTO.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,PasswordEncoder passwordEncoder,EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
    }

    @Transactional
    public UserResponse createUser(String username, String password, String email, String phone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Kullanıcı adı zaten mevcut: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("Phone already exists: " + phone);
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setRole(Role.USER);
        User savedUser = userRepository.save(newUser);
        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getPhone());
    }

    @Transactional
    public UserResponse updateUser(Long id, String username, String password, String email, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı: " + id));
        user.setUsername(username);
        // Şifreyi sadece anlamlı bir değer (boş string veya null değil) olduğunda güncelle
        if (password != null && !password.trim().isEmpty() && password.length() > 0) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setEmail(email);
        user.setPhone(phone);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(), updatedUser.getPhone());
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getPhone()));
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public void deleteUser(Long id) {
    	refreshTokenRepository.deleteByUserId(id);
        // Sonra kullanıcıyı sil
        userRepository.deleteById(id);
    }

    public void sendResetLink(String emailOrPhone) {
        User user = userRepository.findByEmail(emailOrPhone)
                .orElseGet(() -> userRepository.findByPhone(emailOrPhone)
                        .orElseThrow(() -> new IllegalArgumentException("No account found with this email or phone")));

        // 6 haneli rastgele kod oluştur
        String resetCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken token = new PasswordResetToken(resetCode, user, expiryDate);
        tokenRepository.save(token);

        try {
            emailService.sendResetCodeEmail(user.getEmail(), resetCode);
        } catch (Exception e) {
            throw new RuntimeException("E-posta gönderimi başarısız: " + e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz sıfırlama kodu"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Sıfırlama kodu süresi dolmuş");
        }

        User user = resetToken.getUser();
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Yeni şifre en az 8 karakter olmalı");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token'ı geçersiz kıl
        tokenRepository.delete(resetToken);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getPhone());
    }
}