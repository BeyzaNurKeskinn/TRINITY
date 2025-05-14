
package com.project.Trinity.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
//Kimlik doğrulaması gereken endpoint’leri sunar (user ve admin).
    private static final Logger logger = LoggerFactory.getLogger(ProtectedController.class);

    @GetMapping("/user")
    public ResponseEntity<String> userEndpoint() {
        logger.info("User accessed /api/protected/user");
        return ResponseEntity.ok("Bu endpoint, kimlik doğrulaması yapılmış kullanıcılar tarafından erişilebilir.");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        logger.info("User accessed /api/protected/admin");
        return ResponseEntity.ok("Bu endpoint yalnızca ADMIN rolüne sahip kullanıcılar tarafından erişilebilir.");
    }
}