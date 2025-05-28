package com.project.Trinity.Service;

import com.project.Trinity.Entity.PasswordResetToken;
import com.project.Trinity.Entity.Role;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Entity.AuditLog;
import com.project.Trinity.Entity.Status;
import com.project.Trinity.Repository.PasswordResetTokenRepository;
import com.project.Trinity.Repository.RefreshTokenRepository;
import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Repository.AuditLogRepository;
import com.project.Trinity.DTO.UserResponse;
import com.project.Trinity.Service.PasswordService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PasswordService passwordService,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.passwordService = passwordService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        // Hesap INACTIVE ve frozenAt 30 günden eski değilse, aktif hale getir
        if (user.getStatus() == Status.INACTIVE && user.getFrozenAt() != null) {
            long daysFrozen = ChronoUnit.DAYS.between(user.getFrozenAt(), LocalDateTime.now());
            if (daysFrozen < 30) {
                user.setStatus(Status.ACTIVE);
                user.setFrozenAt(null); // Dondurma zamanını sıfırla
                userRepository.save(user);

                // Denetim kaydı ekle
                AuditLog auditLog = new AuditLog();
                auditLog.setAction("Hesap aktif hale getirildi: " + username);
                auditLog.setTimestamp(LocalDateTime.now());
                auditLogRepository.save(auditLog);
            }
        }

        return user;
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

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Kullanıcı eklendi: " + username);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getPhone());
    }

    public UserResponse updateUser(Long id, String newUsername, String password, String email, String phone, String status, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + id));
        user.setUsername(newUsername);
        if (password != null && !password.trim().isEmpty() && password.length() > 0) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setEmail(email);
        user.setPhone(phone);
        if (status != null) {
            user.setStatus(Status.valueOf(status));
        }
        if (role != null) {
            user.setRole(Role.valueOf(role));
        }
        User updatedUser = userRepository.save(user);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Kullanıcı güncellendi: " + newUsername);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

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
        userRepository.deleteById(id);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Kullanıcı silindi: ID " + id);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void uploadProfilePicture(String username, byte[] imageData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
        user.setProfilePicture(imageData);
        userRepository.save(user);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Profil resmi güncellendi: " + username);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void freezeAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
        user.setStatus(Status.INACTIVE); // FROZEN yerine INACTIVE kullanıyoruz
        user.setFrozenAt(LocalDateTime.now());
        userRepository.save(user);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Hesap donduruldu: " + username);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public void sendResetLink(String emailOrPhone) {
        User user = userRepository.findByEmail(emailOrPhone)
                .orElseGet(() -> userRepository.findByPhone(emailOrPhone)
                        .orElseThrow(() -> new IllegalArgumentException("No account found with this email or phone")));

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

        AuditLog auditLog = new AuditLog();
        auditLog.setAction("Şifre sıfırlandı: " + user.getUsername());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        tokenRepository.delete(resetToken);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getPhone());
    }

    public String getCurrentAdminUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public long getTotalPasswordCount() {
        return passwordService.countPasswords();
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public List<String> getRecentActions() {
        return auditLogRepository.findTop10ByOrderByTimestampDesc()
                .stream()
                .map(AuditLog::getAction)
                .collect(Collectors.toList());
    }
 // Sık görüntülenen şifreleri döndür
    public List<String> getMostViewedPasswords(String username) {
        // Gerçek implementasyonda veritabanından en çok görüntülenen şifreleri al
        return List.of("Şifre1", "Şifre2", "Şifre3");
    }
    public List<String> getFeaturedPasswords(String username) {
        return List.of("Şifre1", "Şifre2");
    }
}