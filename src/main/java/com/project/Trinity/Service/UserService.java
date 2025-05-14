
package com.project.Trinity.Service;

import com.project.Trinity.Entity.Role;
import com.project.Trinity.Entity.User;
import com.project.Trinity.Repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {//Kullanıcı oluşturma ve yükleme işlemlerini yönetir.

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
    }
    @Transactional
    public void createUser(String username, String password, String email, String phone) {
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
        newUser.setRole(Role.USER); // Varsayılan rol
        userRepository.save(newUser);
    }
    public void sendResetLink(String emailOrPhone) {
        User user = userRepository.findByEmail(emailOrPhone)
                .orElseGet(() -> userRepository.findByPhone(emailOrPhone)
                        .orElseThrow(() -> new IllegalArgumentException("No account found with this email or phone")));
        // Gerçekte e-posta veya SMS gönderimi burada olur
        System.out.println("Sending reset link to: " + emailOrPhone);
    }
}