package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.UsuarioEntity;

import java.util.List;

/**
 * UsuarioService
 *
 * Purpose:
 * - Defines the service layer interface for managing UsuarioEntity (user) operations.
 * - Provides methods for CRUD operations, registration, password management, and role-based filtering.
 *
 * Notes:
 * - All methods should be implemented with proper transactional context in the implementation class.
 * - Password handling (hashing) is expected to use BCrypt when creating or updating a user.
 * - Case-insensitive searches are applied where appropriate (e.g., findByCorreo, existsByCorreo).
 */
public interface UsuarioService {

    /**
     * Retrieve all users in the system.
     *
     * @return List of all UsuarioEntity objects.
     */
    List<UsuarioEntity> findAll();

    /**
     * Find a user by their unique ID.
     *
     * @param id The primary key of the user.
     * @return The corresponding UsuarioEntity, or null if not found.
     */
    UsuarioEntity findById(Long id);

    /**
     * Find a user by their email address (case-insensitive).
     *
     * @param correo Email of the user.
     * @return The corresponding UsuarioEntity, or null if the email does not exist.
     */
    UsuarioEntity findByCorreo(String correo);

    /**
     * Check whether a user with the given email exists (case-insensitive).
     *
     * @param correo Email to check.
     * @return True if a user with this email exists; false otherwise.
     */
    boolean existsByCorreo(String correo);

    /**
     * Register a new user.
     *
     * Process:
     * - Validates that the email does not already exist.
     * - Hashes the plaintext password using BCrypt.
     * - Persists the new UsuarioEntity with the specified role ID.
     *
     * @param correo       Email address of the new user.
     * @param nombreUsuario Display name / username.
     * @param passwordPlano Plaintext password.
     * @param idRol        Role ID to assign to the user.
     * @return The persisted UsuarioEntity.
     * @throws IllegalArgumentException if the email already exists.
     */
    UsuarioEntity registrar(String correo, String nombreUsuario, String passwordPlano, Long idRol);

    /**
     * Save or update a user entity.
     *
     * Notes:
     * - If the password is provided in plaintext in the 'clave' property, it will be automatically hashed.
     *
     * @param usuario The UsuarioEntity to save or update.
     * @return The persisted or updated UsuarioEntity.
     */
    UsuarioEntity save(UsuarioEntity usuario);

    /**
     * Delete a user by their ID.
     *
     * @param id The primary key of the user to delete.
     */
    void deleteById(Long id);

    /**
     * Change a user's password.
     *
     * Process:
     * - Receives the new password in plaintext.
     * - Hashes the new password before persisting.
     *
     * @param id             User ID whose password is to be changed.
     * @param nuevaContrasena New password in plaintext.
     */
    void changePassword(Long id, String nuevaContrasena);

    /**
     * Retrieve all users with the "provider" role.
     *
     * @return List of UsuarioEntity objects who are providers.
     */
    List<UsuarioEntity> findAllProveedores();
}

/*
Summary (Technical Note):
UsuarioService defines the contract for managing user accounts within the system. 
It includes methods for CRUD operations, registration with BCrypt password hashing, password updates, 
email-based queries, and filtering users by roles (e.g., providers). 
Implementations are expected to handle transactional context and ensure secure password management.
*/
