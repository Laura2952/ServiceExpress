package com.usta.serviexpress.security;

import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UsuarioEntity user;

    public CustomUserDetails(UsuarioEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = user.getRol() != null ? user.getRol().getRol() : "CLIENTE";
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
    }

    @Override public String getPassword() { return user.getClave(); }
    @Override public String getUsername() { return user.getCorreo(); }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    public UsuarioEntity getUser() { return user; }
}