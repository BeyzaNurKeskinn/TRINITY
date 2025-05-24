package com.project.Trinity.Service;

import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Password;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.CategoryRepository;
import com.project.Trinity.Repository.PasswordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private final PasswordRepository passwordRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordRepository passwordRepository, CategoryRepository categoryRepository, PasswordEncoder passwordEncoder) {
        this.passwordRepository = passwordRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Password savePassword(Long id, Long categoryId, String title, String username, String rawPassword, String status, String description) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Aktif kategori bulunamadı: " + categoryId));

        Password password;
        if (id != null) {
            password = passwordRepository.findById(id)
                    .filter(p -> p.getCreatedBy().getId().equals(currentUser.getId()))
                    .filter(p -> p.getStatus() == Status.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("Aktif şifre bulunamadı veya yetkiniz yok: " + id));
            logger.info("Şifre güncelleniyor: id={}, başlık={}", id, title);
        } else {
            password = new Password();
            password.setUser(currentUser);
            password.setCreatedBy(currentUser);
            logger.info("Yeni şifre oluşturuluyor: başlık={}", title);
        }

        password.setCategory(category);
        password.setTitle(title);
        password.setUsername(username);
        if (rawPassword != null && !rawPassword.isBlank()) {
            password.setPassword(passwordEncoder.encode(rawPassword));
        }
        password.setDescription(description);
        password.setStatus(status != null ? Status.valueOf(status) : Status.ACTIVE);

        return passwordRepository.save(password);
    }

    @Transactional(readOnly = true)
    public List<Password> getUserPasswords() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return passwordRepository.findByCreatedByAndStatus(currentUser, Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Password> getPasswordsByCategory(String categoryName) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return passwordRepository.findByUserAndCategoryName(currentUser, categoryName);
    }

    @Transactional(readOnly = true)
    public List<String> getUserCategories() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return passwordRepository.findDistinctCategoryByUser(currentUser);
    }
    public Password updatePassword(Long id, Long categoryId, String title, String username, String password, String status, String description) {
        Password existingPassword = passwordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Şifre bulunamadı: " + id));
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Kategori bulunamadı: " + categoryId));
        existingPassword.setCategory(category);
        existingPassword.setTitle(title);
        existingPassword.setUsername(username);
        existingPassword.setPassword(password); // Şifreleme yapıyorsanız, burada şifrelemeyi unutmayın
        existingPassword.setStatus(Status.valueOf(status));
        existingPassword.setDescription(description);
        return passwordRepository.save(existingPassword);
    }

    @Transactional
    public void deletePassword(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Password password = passwordRepository.findById(id)
                .filter(p -> p.getCreatedBy().getId().equals(currentUser.getId()))
                .filter(p -> p.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Aktif şifre bulunamadı veya yetkiniz yok: " + id));

        password.setStatus(Status.INACTIVE);
        passwordRepository.save(password);
        logger.info("Şifre pasif edildi: id={}", id);
    }

    public long countPasswords() {
        return passwordRepository.count();
    }
}