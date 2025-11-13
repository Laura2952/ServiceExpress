package com.usta.serviexpress.security;

import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetails
 *
 * Purpose:
 * - Implements Spring Security's UserDetails interface to integrate the application's
 *   UsuarioEntity with Spring Security authentication mechanisms.
 * - Wraps a domain user entity (UsuarioEntity) and exposes its credentials, username, and roles
 *   in a format understood by Spring Security.
 *
 * Important Considerations:
 * - The roles from UsuarioEntity are mapped to Spring Security authorities with "ROLE_" prefix.
 * - All account-related checks (expired, locked, credentials expired, enabled) are currently
 *   hardcoded as 'true'. Adjust if implementing account state restrictions.
 */
public class CustomUserDetails implements UserDetails {

    /**
     * Domain user entity wrapped by this UserDetails implementation.
     */
    private final UsuarioEntity user;

    /**
     * Constructor
     *
     * @param user The UsuarioEntity to wrap for Spring Security authentication.
     */
    public CustomUserDetails(UsuarioEntity user) {
        this.user = user;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return Collection of GrantedAuthority objects representing roles.
     *
     * Notes:
     * - Uses the role from the UsuarioEntity.
     * - Defaults to "CLIENTE" if no role is assigned.
     * - Prefixes the role with "ROLE_" to comply with Spring Security conventions.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = user.getRol() != null ? user.getRol().getRol() : "CLIENTE";
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
    }

    /**
     * Returns the password used for authentication.
     *
     * @return Encoded password from UsuarioEntity.
     */
    @Override
    public String getPassword() { return user.getClave(); }

    /**
     * Returns the username used for authentication.
     *
     * @return Email from UsuarioEntity.
     */
    @Override
    public String getUsername() { return user.getCorreo(); }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true (account is non-expired). Adjust if account expiration is implemented.
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true (account is non-locked). Adjust if locking is implemented.
     */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /**
     * Indicates whether the user's credentials (password) have expired.
     *
     * @return true (credentials are non-expired). Adjust if credential expiration is implemented.
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true (user is enabled). Adjust if user activation/deactivation is implemented.
     */
    @Override
    public boolean isEnabled() { return true; }

    /**
     * Returns the wrapped domain user entity.
     *
     * @return UsuarioEntity instance.
     */
    public UsuarioEntity getUser() { return user; }
}

/*
Summary (Technical Note):
CustomUserDetails adapts the application's UsuarioEntity to Spring Security's UserDetails
interface. It provides username, password, and role-based authorities for authentication
and authorization purposes. Currently, all account status checks are always true, and the
role is mapped with a "ROLE_" prefix. This class is used by CustomUserDetailsService
to load authenticated users.
*/
