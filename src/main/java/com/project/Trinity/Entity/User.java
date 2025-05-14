
package com.project.Trinity.Entity;

import jakarta.persistence.*;
import lombok.Data;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {//Kullanıcı bilgilerini temsil eden varlık sınıfı. Spring Security ile çalışır.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    public User() {
    }

   

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }//Kullanıcının yetkilerini döner (örneğin, ROLE_USER).
    //Neden?: Spring Security, rollerle yetkilendirme yapar.

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}