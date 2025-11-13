package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.RegistroClienteDTO;
import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.RolRepository;
import com.usta.serviexpress.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService
 *
 * Purpose:
 * - Handles authentication-related business logic, including user registration.
 * - Encapsulates logic for validating input, checking uniqueness, assigning roles, and saving users.
 *
 * Notes:
 * - Uses Spring's @Transactional to ensure that registration is atomic.
 * - Uses PasswordEncoder to securely hash passwords before storing.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new client user in the system.
     *
     * Steps:
     * 1) Validates that the password and confirmation password match.
     * 2) Ensures the email is unique (case-insensitive).
     * 3) Retrieves or creates the "CLIENTE" role.
     * 4) Builds the UsuarioEntity and persists it to the database.
     *
     * @param dto Data Transfer Object containing registration information from the client.
     *            - dto.getNombre(): client's name
     *            - dto.getCorreo(): email address
     *            - dto.getTelefono(): phone number
     *            - dto.getCiudad(): city
     *            - dto.getPassword(): password
     *            - dto.getConfirmPassword(): password confirmation
     * @return UsuarioEntity the newly created user entity
     *
     * @throws IllegalArgumentException if passwords do not match or email already exists
     */
    @Transactional
    public UsuarioEntity registrarCliente(RegistroClienteDTO dto) {
        // 1) Validate that password and confirmation match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 2) Ensure email is unique (case-insensitive)
        if (usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        // 3) Retrieve or create "CLIENTE" role
        RolEntity rolCliente = rolRepository.findByRolIgnoreCase("CLIENTE")
                .orElseGet(() -> {
                    RolEntity r = new RolEntity();
                    r.setRol("CLIENTE");
                    return rolRepository.save(r);
                });

        // 4) Construct and populate the UsuarioEntity
        UsuarioEntity u = new UsuarioEntity();
        u.setNombreUsuario(dto.getNombre());
        u.setCorreo(dto.getCorreo().toLowerCase()); // store email in lowercase
        u.setTelefono(dto.getTelefono());
        u.setCiudad(dto.getCiudad());
        u.setClave(passwordEncoder.encode(dto.getPassword())); // encode password
        u.setRol(rolCliente);

        // Persist and return the new user
        return usuarioRepository.save(u);
    }
}

/**
 * Summary:
 * AuthService provides client registration functionality.
 * Ensures email uniqueness, password validation, and proper role assignment.
 * All operations are transactional to maintain data integrity.
 */
