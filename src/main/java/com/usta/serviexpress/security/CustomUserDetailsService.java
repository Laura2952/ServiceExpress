package com.usta.serviexpress.security;

import com.usta.serviexpress.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService
 *
 * Purpose:
 * - Implements Spring Security's UserDetailsService interface.
 * - Responsible for loading user-specific data during authentication.
 * - Bridges the application's UsuarioRepository with Spring Security.
 *
 * Important Notes:
 * - The service fetches the UsuarioEntity including its role eagerly (fetchRol)
 *   to avoid LazyInitializationExceptions when retrieving authorities.
 * - Throws UsernameNotFoundException if the user is not found, which Spring Security
 *   will handle as a failed login attempt.
 * - Marked as @Transactional(readOnly = true) to ensure read-only transactional context.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository used to retrieve user entities from the database.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Locates the user based on the username (email in this case).
     *
     * @param username The username identifying the user (email).
     * @return UserDetails implementation (CustomUserDetails) for Spring Security.
     * @throws UsernameNotFoundException if the user is not found in the database.
     *
     * Notes:
     * - Uses a case-insensitive query to find the user by email.
     * - Eagerly fetches the user's role to ensure authorities are available immediately.
     * - This method is called automatically by Spring Security during authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByCorreoIgnoreCaseFetchRol(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return new CustomUserDetails(usuario);
    }
}

/*
Summary (Technical Note):
CustomUserDetailsService integrates the application's UsuarioEntity with Spring Security's
authentication mechanism. It retrieves a user by email from the database (case-insensitive),
fetches the associated role eagerly, and wraps it in a CustomUserDetails object for Spring
Security. This class ensures that authentication can access the user's credentials and
authorities safely.
*/
