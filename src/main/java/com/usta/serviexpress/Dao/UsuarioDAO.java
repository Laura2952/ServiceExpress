package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * UsuarioDAO
 *
 * Purpose:
 * - Data Access Object (DAO) for performing CRUD operations and custom queries on UsuarioEntity.
 * - Extends CrudRepository to provide basic CRUD operations.
 * - Provides custom methods for changing a user's password and finding a user by email.
 *
 * Type parameters:
 * - UsuarioEntity: The entity type managed by this repository.
 * - Long: Type of the entity's primary key.
 *
 * Transactional and modifying notes:
 * - Update operations are annotated with @Transactional and @Modifying to manage the persistence context.
 * - Queries are JPQL-based, targeting UsuarioEntity fields.
 */
public interface UsuarioDAO extends CrudRepository<UsuarioEntity, Long> {

    /**
     * Update the password of a specific user.
     *
     * @param idUsuario ID of the user whose password is to be updated.
     * @param nuevaContrasena The new password to set (should be hashed before passing).
     *
     * Notes:
     * - This method directly updates the database; it does not modify the entity in the current persistence context.
     * - Ensure proper password hashing/security before calling.
     */
    @Transactional
    @Modifying
    @Query("UPDATE UsuarioEntity SET clave = ?2 WHERE idUsuario = ?1")
    void changePassword(Long idUsuario, String nuevaContrasena);

    /**
     * Retrieve a user entity by its email.
     *
     * @param correo Email address of the user.
     * @return The matching UsuarioEntity, or null if not found.
     *
     * Notes:
     * - Emails are assumed to be unique in the system.
     * - The returned entity is managed and can be used in transactional operations.
     */
    @Transactional
    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo = ?1")
    UsuarioEntity findByEmail(String correo);
}

/*
Summary (Technical Note):
UsuarioDAO is a Spring Data repository for managing UsuarioEntity instances. It provides:
- Basic CRUD operations via CrudRepository.
- Custom methods to update a user's password and retrieve a user by email.
Transactional and modifying annotations ensure proper database updates and read consistency.
Password updates should be performed with hashed values for security.
*/
