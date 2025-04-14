package com.project.Trinity.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
//Dosyanın Amacı: Şifreleri güvenli bir şekilde şifrelemek için PasswordEncoder’ı yapılandırır.
/*
 * Neden?: Kullanıcı şifrelerini düz metin olarak saklamak güvenlik açığıdır. 
 * BCrypt, güçlü ve güvenli bir şifreleme sağlar.
 * Bu dosya, şifrelerin güvenli saklanması için gerekli. Spring Security ile çalışırken, 
 * şifreleri şifrelemek ve doğrulamak için PasswordEncoder kullanıyoruz
 */