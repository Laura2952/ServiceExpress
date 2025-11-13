package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ServicioDAO
 *
 * Purpose:
 * - Data Access Object (DAO) for performing CRUD operations and custom queries on ServicioEntity.
 * - Provides methods to retrieve services by ID, status, or name, and to update or remove services.
 *
 * Type parameters:
 * - ServicioEntity: The entity type managed by this repository.
 * - Long: Type of the entity's primary key.
 *
 * Transactional and modifying notes:
 * - Read-only queries use @Transactional(readOnly = true) for performance optimization.
 * - Update and delete operations use @Modifying and @Transactional annotations to manage persistence context properly.
 * - 'clearAutomatically' and 'flushAutomatically' ensure changes are immediately applied and the context is refreshed.
 */
public interface ServicioDAO extends JpaRepository<ServicioEntity, Long> {

    /**
     * Retrieve a ServicioEntity by its primary key (idServicio).
     *
     * @param idServicio Service ID.
     * @return The matching ServicioEntity, or null if not found.
     */
    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE S.idServicio = ?1")
    ServicioEntity viewDetail(Long idServicio);

    /**
     * Retrieve all services with a given status.
     *
     * @param estado Status to filter by (e.g., "ACTIVE", "INACTIVE").
     * @return List of ServicioEntity objects matching the status.
     */
    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE S.estado = ?1")
    List<ServicioEntity> listByEstado(String estado);

    /**
     * Search for services by name using a case-insensitive partial match.
     *
     * @param nombre Name substring to search for.
     * @return List of ServicioEntity objects whose names contain the given substring.
     */
    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE LOWER(S.nombre) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<ServicioEntity> searchByNombre(String nombre);

    /**
     * Update basic information of a service.
     *
     * @param idServicio Service ID to update.
     * @param nuevoNombre New service name.
     * @param nuevaDescripcion New service description.
     * @param nuevoPrecio New service price.
     * @param nuevoEstado New service status.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ServicioEntity S SET S.nombre = ?2, S.descripcion = ?3, S.precio = ?4, S.estado = ?5 WHERE S.idServicio = ?1")
    void updateServicio(Long idServicio, String nuevoNombre, String nuevaDescripcion, Double nuevoPrecio, String nuevoEstado);

    /**
     * Delete a service by its primary key.
     *
     * @param idServicio Service ID to delete.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ServicioEntity S WHERE S.idServicio = ?1")
    void removeById(Long idServicio);
}

/*
Summary (Technical Note):
ServicioDAO is a Spring Data JPA repository for managing ServicioEntity instances. It provides:
- Retrieval of services by ID, status, or name (with case-insensitive search).
- Update of service properties (name, description, price, status) with immediate persistence context synchronization.
- Deletion of services by ID.
Transactional annotations optimize read-only queries and ensure proper handling of update/delete operations.
*/
