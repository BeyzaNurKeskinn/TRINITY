package com.project.Trinity.Controller;

import com.project.Trinity.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        String username = authentication.getName(); // Kullanıcı adını al

        data.put("adminName", userService.getCurrentAdminUsername());
        data.put("passwordCount", userService.getTotalPasswordCount());
        data.put("userCount", userService.getTotalUserCount());
        data.put("recentActions", userService.getRecentActions());
        data.put("featuredPasswords", userService.getFeaturedPasswords(username)); // username ile çağır

        return ResponseEntity.ok(data);
    }
}