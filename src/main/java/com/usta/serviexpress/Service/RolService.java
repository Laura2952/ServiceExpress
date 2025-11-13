package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.RolEntity;

import java.util.List;

/**
 * RolService
 *
 * Purpose:
 * - Defines the contract for managing role entities (RolEntity) in the system.
 * - Provides basic CRUD operations: list all, find by ID, save, and delete.
 *
 * Notes:
 * - Implementations may include additional business logic, validation, or transactional handling.
 * - Roles are assumed to be system-wide definitions of user permissions or access levels.
 */
public interface RolService {

    /**
     * Retrieves all roles in the system.
     *
     * @return List of RolEntity objects representing all roles
     */
    List<RolEntity> findAll();

    /**
     * Finds a role by its unique identifier.
     *
     * @param id The primary key of the role
     * @return RolEntity object if found
     * @throws RuntimeException if role with given id does not exist (implementation dependent)
     */
    RolEntity findById(Long id);

    /**
     * Persists a new role or updates an existing one.
     *
     * @param rol RolEntity object to save or update
     *
     * Notes:
     * - Implementation may distinguish between create and update based on the presence of an ID.
     * - Validation (e.g., unique role name) may be performed in the service layer.
     */
    void save(RolEntity rol);

    /**
     * Deletes a role by its unique identifier.
     *
     * @param id The primary key of the role to delete
     *
     * Notes:
     * - Implementations should handle referential integrity (e.g., prevent deletion if role is assigned to users).
     */
    void deleteById(Long id);
}

/*
Summary (Technical Note):
RolService defines a simple contract for managing role entities within the system. 
It exposes methods to retrieve all roles, find a role by ID, save or update a role, 
and delete a role by ID. Implementations are expected to handle validations, 
transactional integrity, and any business rules associated with role management.
*/
