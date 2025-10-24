// src/main/java/com/usta/serviexpress/config/SecurityConfig.java
package com.usta.serviexpress.config;

import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.security.CustomUserDetails;
import com.usta.serviexpress.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            // ====== Guarda el usuario de dominio en sesión para tu controlador ======
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails cud) {
                UsuarioEntity usuario = cud.getUser();
                request.getSession().setAttribute("usuarioSesion", usuario);
            }
            // =======================================================================

            Set<String> roles = authentication.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/Admins/usuarios");
            } else if (roles.contains("ROLE_PROVEEDOR")) {
                response.sendRedirect("/proveedor/servicios");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AntPathRequestMatcher webhookMatcher = new AntPathRequestMatcher("/webhooks/wompi");

        http
                // CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers(webhookMatcher))

                // tu provider
                .authenticationProvider(authenticationProvider(passwordEncoder()))

                // ===== Rutas públicas y protegidas =====
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/favicon.ico",
                                "/auth/**",
                                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
                                "/checkout/**",
                                "/pagos/**",
                                "/webhooks/wompi",
                                "/error"
                        ).permitAll()

                        // zonas con rol
                        .requestMatchers("/Admins/**").hasRole("ADMIN")
                        .requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")

                        .anyRequest().authenticated()
                )

                // ===== Login/Logout =====
                .formLogin(login -> login
                        .loginPage("/auth/login").permitAll()
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/auth/login?error=true")
                )
                .logout(l -> l
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                )
                .headers(Customizer.withDefaults());

        return http.build();
    }

}

/*
 * Nota importante para el webhook de Wompi:
 * - El header de firma puede variar por versión de Wompi. Revisa tus logs: puede llegar como
 *   "X-Event-Signature", "Integrity-Signature" u otro similar.
 * - Valida SIEMPRE la firma: HMAC-SHA256 del cuerpo crudo (raw body) usando tu eventsSecret.
 *   Si la firma no coincide, responde 401 y no proceses el evento.
 */