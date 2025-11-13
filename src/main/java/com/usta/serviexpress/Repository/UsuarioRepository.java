package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UsuarioRepository
 *
 * Purpose:
 * - Repository interface for performing CRUD operations and custom queries on UsuarioEntity.
 * - Provides methods to find users by email, check existence, and retrieve users by role.
 * - Includes queries with optional eager fetching of the related RolEntity.
 *
 * Notes:
 * - Extends JpaRepository to leverage standard JPA methods.
 * - Uses Optional for queries that may or may not return a result.
 * - Supports case-insensitive searches for email.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Finds a user by email (case-sensitive).
     *
     * @param correo the email address to search
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<UsuarioEntity> findByCorreo(String correo);

    /**
     * Checks if a user exists with the given email (case-sensitive).
     *
     * @param correo the email address to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByCorreo(String correo);

    /**
     * Finds a user by email (case-insensitive).
     *
     * @param correo the email address to search
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<UsuarioEntity> findByCorreoIgnoreCase(String correo);

    /**
     * Checks if a user exists with the given email (case-insensitive).
     *
     * @param correo the email address to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByCorreoIgnoreCase(String correo);

    /**
     * Finds a user by email (case-insensitive) and eagerly fetches the associated role.
     *
     * @param correo the email address to search
     * @return Optional containing the user with role loaded if found, empty otherwise
     *
     * Notes:
     * - Useful to avoid lazy loading when accessing the user's role immediately after fetching.
     */
    @Query("""
       select u
       from UsuarioEntity u
       left join fetch u.rol
       where lower(u.correo) = lower(:correo)
       """)
    Optional<UsuarioEntity> findByCorreoIgnoreCaseFetchRol(@Param("correo") String correo);

    /**
     * Finds all users with a specific role.
     *
     * @param rol the name of the role
     * @return List of users with the specified role
     */
    List<UsuarioEntity> findByRol_Rol(String rol);
}

/**
 * Summary:
 * UsuarioRepository provides standard and custom queries for managing UsuarioEntity objects.
 * Supports finding users by email, checking existence, and filtering by role.
 * Includes eager-fetching query for roles to prevent lazy-loading issues when role information is needed.
 */
