package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.RolRepository;
import com.usta.serviexpress.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UsuarioServiceImplement
 *
 * Purpose:
 * - Provides the concrete implementation of UsuarioService for managing user accounts.
 * - Handles CRUD operations, registration, password hashing, and role-based queries.
 *
 * Notes:
 * - Uses Spring's @Transactional to ensure data consistency for operations that modify the database.
 * - Passwords are hashed using a PasswordEncoder (BCrypt recommended).
 * - Implements safeguards to prevent duplicate emails and invalid password entries.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImplement implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieve all users.
     *
     * @return List of all UsuarioEntity objects.
     */
    @Override
    public List<UsuarioEntity> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Find a user by their ID.
     *
     * @param id User ID.
     * @return Corresponding UsuarioEntity, or null if not found.
     */
    @Override
    public UsuarioEntity findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Find a user by their email (case-insensitive).
     *
     * Notes:
     * - Also fetches the associated RolEntity to avoid lazy-loading issues.
     *
     * @param correo Email to search.
     * @return UsuarioEntity if found, null otherwise.
     */
    @Override
    public UsuarioEntity findByCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCaseFetchRol(correo).orElse(null);
    }

    /**
     * Check if a user exists by email (case-insensitive).
     *
     * @param correo Email to check.
     * @return True if a user with the email exists, false otherwise.
     */
    @Override
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreoIgnoreCase(correo);
    }

    /**
     * Register a new user.
     *
     * Process:
     * - Validates required fields (email, password) and uniqueness of email.
     * - Retrieves the role by ID; throws exception if role not found.
     * - Hashes the plaintext password using the configured PasswordEncoder.
     * - Persists the new user entity.
     *
     * @param correo        Email address.
     * @param nombreUsuario Display name (optional).
     * @param passwordPlano Plaintext password.
     * @param idRol         Role ID to assign.
     * @return Persisted UsuarioEntity.
     * @throws IllegalArgumentException on missing/invalid input or duplicate email.
     */
    @Override
    @Transactional
    public UsuarioEntity registrar(String correo, String nombreUsuario, String passwordPlano, Long idRol) {
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("Correo es obligatorio");
        if (passwordPlano == null || passwordPlano.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        if (existsByCorreo(correo))
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");

        RolEntity rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        UsuarioEntity u = new UsuarioEntity();
        u.setCorreo(correo.trim());
        u.setNombreUsuario(nombreUsuario != null ? nombreUsuario.trim() : null);
        u.setClave(passwordEncoder.encode(passwordPlano));
        u.setRol(rol);

        return usuarioRepository.save(u);
    }

    /**
     * Save or update a user entity.
     *
     * Notes:
     * - If the password ('clave') is in plaintext, hashes it automatically.
     * - Detects if the password is already hashed (BCrypt prefix) to avoid double-hashing.
     *
     * @param usuario UsuarioEntity to persist.
     * @return Saved or updated UsuarioEntity.
     */
    @Override
    @Transactional
    public UsuarioEntity save(UsuarioEntity usuario) {
        String p = usuario.getClave();
        if (p != null && !p.isBlank()
                && !p.startsWith("$2a$") && !p.startsWith("$2b$") && !p.startsWith("$2y$")) {
            usuario.setClave(passwordEncoder.encode(p));
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Delete a user by ID.
     *
     * @param id User ID.
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Change the password for a user.
     *
     * Process:
     * - Validates the new password is not empty.
     * - Retrieves the user by ID; throws exception if not found.
     * - Hashes the new password before saving.
     *
     * @param id             User ID.
     * @param nuevaContrasena New plaintext password.
     */
    @Override
    @Transactional
    public void changePassword(Long id, String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.isBlank())
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");

        UsuarioEntity u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setClave(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(u);
    }

    /**
     * Retrieve all users with the "PROVEEDOR" role.
     *
     * Notes:
     * - Assumes role names in the database are stored as uppercase "PROVEEDOR".
     *
     * @return List of UsuarioEntity objects representing providers.
     */
    @Override
    public List<UsuarioEntity> findAllProveedores() {
        return usuarioRepository.findByRol_Rol("PROVEEDOR");
    }
}

/*
Summary (Technical Note):
UsuarioServiceImplement provides a full implementation of the UsuarioService interface. 
It manages user CRUD operations, registration with password hashing (BCrypt), role assignment, 
password updates, and filtering users by role (e.g., providers). 
All operations that modify the database are transactional to maintain consistency. 
Plaintext passwords are automatically hashed before persistence; the service prevents duplicate emails and enforces required fields.
*/
