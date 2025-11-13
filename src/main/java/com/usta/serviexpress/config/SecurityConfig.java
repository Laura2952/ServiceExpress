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

/**
 * SecurityConfig
 *
 * Purpose:
 * - Configures Spring Security for the Serviexpress application.
 * - Sets up password encoding, authentication provider, role-based access control,
 *   login/logout behavior, and CSRF exclusions for webhooks.
 *
 * Key Components:
 * - PasswordEncoder: BCrypt with strength 12.
 * - AuthenticationProvider: DAO-based with custom UserDetailsService.
 * - Role-based redirection after successful login.
 * - SecurityFilterChain defining protected routes, public routes, login, logout, and CSRF.
 *
 * Notes:
 * - Session attribute "usuarioSesion" stores the domain UsuarioEntity for use in controllers.
 * - Webhook endpoint /webhooks/wompi is excluded from CSRF protection.
 * - Access restrictions:
 *     * "/Admins/**" → ROLE_ADMIN
 *     * "/proveedor/**" → ROLE_PROVEEDOR or ROLE_ADMIN
 * - Default page redirects:
 *     * ADMIN → /Admins/usuarios
 *     * PROVEEDOR → /proveedor/servicios
 *     * Others → /
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Password encoder bean using BCrypt with strength 12.
     *
     * Purpose:
     * - Encodes and verifies passwords securely.
     * - Required by Spring Security authentication provider.
     *
     * @return PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationProvider using DAO pattern.
     *
     * Purpose:
     * - Integrates Spring Security with the application's CustomUserDetailsService.
     * - Applies the password encoder to authenticate users.
     *
     * @param encoder PasswordEncoder bean to hash/verify passwords.
     * @return configured AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    /**
     * Success handler that redirects users based on role after login.
     *
     * Purpose:
     * - Saves the domain UsuarioEntity in HTTP session for controller access.
     * - Performs role-based redirection:
     *     - ROLE_ADMIN → /Admins/usuarios
     *     - ROLE_PROVEEDOR → /proveedor/servicios
     *     - Default → /
     *
     * @return AuthenticationSuccessHandler
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            // Save domain user in session
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails cud) {
                UsuarioEntity usuario = cud.getUser();
                request.getSession().setAttribute("usuarioSesion", usuario);
            }

            // Redirect based on role
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

    /**
     * Defines the Spring Security filter chain.
     *
     * Purpose:
     * - Configures HTTP security, including route protection, login/logout, and CSRF settings.
     *
     * Key Points:
     * - CSRF is disabled for /webhooks/wompi to allow external POST requests without tokens.
     * - Publicly accessible routes include "/", "/auth/**", static assets, checkout/payment APIs, webhooks, and "/api/**".
     * - Routes under "/Admins/**" require ROLE_ADMIN.
     * - Routes under "/proveedor/**" require ROLE_PROVEEDOR or ROLE_ADMIN.
     * - All other routes require authentication.
     * - Custom login page: /auth/login
     * - Login processing URL: /auth/login
     * - Logout URL: /auth/logout
     *
     * @param http HttpSecurity builder.
     * @return configured SecurityFilterChain
     * @throws Exception if security configuration fails.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF exclusion for webhook
        AntPathRequestMatcher webhookMatcher = new AntPathRequestMatcher("/webhooks/wompi");

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(webhookMatcher))
                .authenticationProvider(authenticationProvider(passwordEncoder()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/favicon.ico",
                                "/auth/**",
                                "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**",
                                "/checkout/**",
                                "/pagos/**",
                                "/webhooks/wompi",
                                "/error",
                                "/api/**"
                        ).permitAll()
                        .requestMatchers("/Admins/**").hasRole("ADMIN")
                        .requestMatchers("/proveedor/**").hasAnyRole("PROVEEDOR", "ADMIN")
                        .anyRequest().authenticated()
                )
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
Summary (Technical Note):
SecurityConfig sets up Spring Security for the Serviexpress application. 
It provides password encoding (BCrypt), authentication via CustomUserDetailsService, 
role-based redirects on login, route protection (public, admin, provider), CSRF exclusion for webhooks, 
and login/logout handling. The domain user is stored in the session for controller access. 
Developers should validate webhook signatures before processing events.
*/
