package com.project.Trinity.Service;

import com.project.Trinity.Entity.Status;
import com.project.Trinity.Entity.Category;
import com.project.Trinity.Entity.Password;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.CategoryRepository;
import com.project.Trinity.Repository.PasswordRepository;
import com.project.Trinity.Util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private final PasswordRepository passwordRepository;
    private final CategoryRepository categoryRepository;
    private final EncryptionUtil encryptionUtil; // Enjekte ediliyor

    public PasswordService(PasswordRepository passwordRepository, CategoryRepository categoryRepository, EncryptionUtil encryptionUtil) {
        this.passwordRepository = passwordRepository;
        this.categoryRepository = categoryRepository;
        this.encryptionUtil = encryptionUtil;
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
            try {
                password.setPassword(encryptionUtil.encrypt(rawPassword)); // AES ile şifreleme
            } catch (Exception e) {
                throw new RuntimeException("Şifre şifreleme hatası: " + e.getMessage());
            }
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
        return passwordRepository.findByUserAndCategoryNameAndStatus(currentUser, categoryName, Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<String> getUserCategories() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return passwordRepository.findDistinctCategoryByUser(currentUser);
    }

    @Transactional
    public Password updatePassword(Long id, Long categoryId, String title, String username, String rawPassword, String status, String description) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Password existingPassword = passwordRepository.findById(id)
            .filter(p -> p.getCreatedBy().getId().equals(currentUser.getId()))
            .filter(p -> p.getStatus() == Status.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Aktif şifre bulunamadı veya yetkiniz yok: " + id));

        Category category = categoryRepository.findById(categoryId)
            .filter(c -> c.getStatus() == Status.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Aktif kategori bulunamadı: " + categoryId));

        existingPassword.setCategory(category);
        existingPassword.setTitle(title);
        existingPassword.setUsername(username);
        if (rawPassword != null && !rawPassword.isBlank()) {
            try {
                existingPassword.setPassword(encryptionUtil.encrypt(rawPassword));
            } catch (Exception e) {
                throw new RuntimeException("Şifre şifreleme hatası: " + e.getMessage());
            }
        }
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

    
    public String getDecryptedPassword(Long id) throws Exception {
        Password password = passwordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Şifre bulunamadı."));
        return encryptionUtil.decrypt(password.getPassword());
    }
    
    @Transactional
    public void incrementViewCount(Long passwordId) {
        Password password = passwordRepository.findById(passwordId)
                .orElseThrow(() -> new IllegalArgumentException("Şifre bulunamadı: " + passwordId));
        password.setViewCount(password.getViewCount() + 1);
        password.setLastUsed(LocalDateTime.now()); // lastUsed alanını da güncelleyelim
        passwordRepository.save(password);
    }

    @Transactional
    public Password toggleFeatured(Long passwordId, boolean isFeatured) {
        Password password = passwordRepository.findById(passwordId)
                .orElseThrow(() -> new IllegalArgumentException("Şifre bulunamadı: " + passwordId));
        logger.info("Öne çıkarma güncelleniyor: id={}, isFeatured={}", passwordId, isFeatured);
        password.setIsFeatured(isFeatured);
        Password savedPassword = passwordRepository.save(password);
        logger.info("Kaydedilen şifre: id={}, isFeatured={}", savedPassword.getId(), savedPassword.getIsFeatured());
        return savedPassword;
    }

    public List<Password> getMostViewedPasswordsByUser(User user, int limit) {
        return passwordRepository.findByUserAndStatusOrderByViewCountDesc(user, Status.ACTIVE)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Password> getFeaturedPasswordsByUser(User user) {
        return passwordRepository.findByUserAndIsFeaturedTrueAndStatus(user, Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Password findById(Long id) {
        return passwordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Şifre bulunamadı: " + id));
    }

    
    
}