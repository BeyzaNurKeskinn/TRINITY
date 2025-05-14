package com.project.Trinity.Service;


import com.project.Trinity.Entity.User;

import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Util.JwtUtil;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.project.Trinity.Entity.RefreshToken;
import com.project.Trinity.Repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {//Refresh token oluşturma, doğrulama ve temizleme işlemlerini yönetir.

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent() && existingToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            return existingToken.get().getToken();
        } else if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
        }

        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);
        logger.info("New refresh token created for user: {}", username);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate() != null && t.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Refresh tokens deleted for user: {}", user.getUsername());
    }

    public String generateNewAccessToken(String refreshToken) {
        return findByToken(refreshToken)
                .map(RefreshToken::getUser)
                .map(user -> jwtUtil.generateToken(user))
                .orElseThrow(() -> new InvalidRefreshTokenException("Geçersiz veya süresi dolmuş yenileme token'ı"));
    }//@Transactional: Veritabanı işlemini güvenli yapar.
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Her gün 02:00’de
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        logger.info("Expired refresh tokens cleaned");
    }
}