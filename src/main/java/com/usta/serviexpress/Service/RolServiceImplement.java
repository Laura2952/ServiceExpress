package com.usta.serviexpress.Service;

import com.usta.serviexpress.Dao.RolDAO;
import com.usta.serviexpress.Entity.RolEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * RolServiceImplement
 *
 * Purpose:
 * - Concrete implementation of the RolService interface.
 * - Provides CRUD operations for RolEntity using RolDAO (Spring Data repository).
 *
 * Notes:
 * - Uses @Autowired to inject the RolDAO repository.
 * - Methods are simple pass-through calls to the DAO; additional business logic can be added if needed.
 */
@Service
public class RolServiceImplement implements RolService {

    /**
     * RolDAO instance injected by Spring.
     * Provides access to the database for role entities.
     */
    @Autowired
    private RolDAO rolDAO;

    /**
     * Retrieves all roles from the database.
     *
     * @return List of RolEntity objects representing all roles
     */
    @Override
    public List<RolEntity> findAll() {
        // Cast to List since rolDAO.findAll() returns an Iterable
        return (List<RolEntity>) rolDAO.findAll();
    }

    /**
     * Finds a role by its unique ID.
     *
     * @param id Primary key of the role
     * @return RolEntity object if found; otherwise null
     *
     * Notes:
     * - Uses Optional to handle the case where the role does not exist.
     */
    @Override
    public RolEntity findById(Long id) {
        Optional<RolEntity> r = rolDAO.findById(id);
        return r.orElse(null); // Returns null if role not found
    }

    /**
     * Saves or updates a role entity in the database.
     *
     * @param rol RolEntity object to persist
     *
     * Notes:
     * - rolDAO.save handles both insert and update operations depending on the presence of an ID.
     * - No additional validation is performed in this method.
     */
    @Override
    public void save(RolEntity rol) {
        rolDAO.save(rol);
    }

    /**
     * Deletes a role by its unique ID.
     *
     * @param id Primary key of the role to delete
     *
     * Notes:
     * - If the ID does not exist, the deletion will be silently ignored by the DAO.
     * - Referential integrity (e.g., roles assigned to users) should be handled elsewhere if needed.
     */
    @Override
    public void deleteById(Long id) {
        rolDAO.deleteById(id);
    }
}

/*
Summary (Technical Note):
RolServiceImplement is a Spring Service that provides CRUD operations for role entities
by delegating to the RolDAO repository. It allows fetching all roles, finding by ID,
saving/updating, and deleting roles. Optional and null handling is used for findById,
and simple pass-through methods are implemented for the other operations.
*/
