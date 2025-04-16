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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private final PasswordRepository passwordRepository;
    private final CategoryRepository categoryRepository;

    public PasswordService(PasswordRepository passwordRepository, CategoryRepository categoryRepository) {
        this.passwordRepository = passwordRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Password savePassword(Long id, Long categoryId, String title, String username, String rawPassword, String status) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Aktif kategori bulunamadı: " + categoryId));

        Password password;
        if (id != null) {
            // Güncelleme
            password = passwordRepository.findById(id)
                    .filter(p -> p.getCreatedBy().getId().equals(currentUser.getId()))
                    .filter(p -> p.getStatus() ==Status.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("Aktif şifre bulunamadı veya yetkiniz yok: " + id));
            logger.info("Şifre güncelleniyor: id={}, başlık={}", id, title);
        } else {
            // Yeni kayıt
            password = new Password();
            password.setCreatedBy(currentUser);
            logger.info("Yeni şifre oluşturuluyor: başlık={}", title);
        }

        password.setCategory(category);
        password.setTitle(title);
        password.setUsername(username);
        if (rawPassword != null && !rawPassword.isBlank()) {
            password.setPassword(rawPassword);
        }
        password.setStatus(status != null ? Status.valueOf(status) : Status.ACTIVE);

        return passwordRepository.save(password);
    }

    @Transactional(readOnly = true)
    public List<Password> getUserPasswords() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return passwordRepository.findByCreatedByAndStatus(currentUser, Status.ACTIVE);
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
}