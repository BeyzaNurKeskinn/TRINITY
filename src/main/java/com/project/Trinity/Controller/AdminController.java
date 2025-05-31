package com.project.Trinity.Controller;

import com.project.Trinity.Repository.PasswordRepository;
import com.project.Trinity.Repository.PasswordResetTokenRepository;
import com.project.Trinity.Repository.UserRepository;
import com.project.Trinity.Service.EmailService;
import com.project.Trinity.Service.PasswordService;
import com.project.Trinity.Service.RefreshTokenService;
import com.project.Trinity.Service.UserService;
import com.project.Trinity.Util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    private final PasswordRepository passwordRepository;
    
    
    public AdminController(PasswordRepository passwordRepository) {
    		this.passwordRepository = passwordRepository;
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        String username = authentication.getName();

        data.put("adminName", userService.getCurrentAdminUsername());
        data.put("passwordCount", userService.getTotalPasswordCount());
        data.put("userCount", userService.getTotalUserCount());
        data.put("recentActions", userService.getRecentActions());
        data.put("featuredPasswords", userService.getFeaturedPasswords(username));

        // Kategorilere göre şifre dağılımı
        List<Object[]> categoryStats = passwordRepository.findPasswordCountByCategory();
        Map<String, Integer> categoryDistribution = categoryStats.stream()
                .collect(Collectors.toMap(
                        stat -> (String) stat[0],
                        stat -> ((Long) stat[1]).intValue()
                ));
        data.put("categoryDistribution", categoryDistribution);

        return ResponseEntity.ok(data);
    }
}