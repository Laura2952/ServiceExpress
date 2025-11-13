package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * SolicitudServicioDAO
 *
 * Purpose:
 * - Data Access Object (DAO) for performing CRUD operations and custom queries on SolicitudServicioEntity.
 * - Provides methods to retrieve service requests by ID, associated service, status, or request date.
 * - Supports updating request status and deleting service requests.
 *
 * Type parameters:
 * - SolicitudServicioEntity: The entity type managed by this repository.
 * - Long: Type of the entity's primary key.
 *
 * Transactional and modifying notes:
 * - Read-only queries use @Transactional(readOnly = true) for performance optimization.
 * - Update and delete operations use @Modifying and @Transactional annotations to manage the persistence context.
 * - 'clearAutomatically' and 'flushAutomatically' ensure that updates/deletes are immediately applied and the persistence context is refreshed.
 */
public interface SolicitudServicioDAO extends JpaRepository<SolicitudServicioEntity, Long> {

    /**
     * Retrieve a SolicitudServicioEntity by its primary key (idSolicitud).
     *
     * @param idSolicitud Service request ID.
     * @return The matching SolicitudServicioEntity, or null if not found.
     */
    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.idSolicitud = ?1")
    SolicitudServicioEntity viewDetail(Long idSolicitud);

    /**
     * Retrieve all service requests associated with a specific service.
     *
     * @param idServicio Service ID to filter by.
     * @return List of SolicitudServicioEntity objects linked to the specified service.
     */
    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.servicio.idServicio = ?1")
    List<SolicitudServicioEntity> listByServicio(Long idServicio);

    /**
     * Retrieve all service requests filtered by their status.
     *
     * @param estado Status to filter by (e.g., "PENDING", "APPROVED", "COMPLETED").
     * @return List of SolicitudServicioEntity objects with the specified status.
     */
    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.estado = ?1")
    List<SolicitudServicioEntity> listByEstado(String estado);

    /**
     * Retrieve all service requests created on a specific date.
     *
     * @param fecha Date of the service requests.
     * @return List of SolicitudServicioEntity objects created on the given date.
     */
    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.fechaSolicitud = ?1")
    List<SolicitudServicioEntity> listByFecha(LocalDate fecha);

    /**
     * Update the status of a specific service request.
     *
     * @param idSolicitud ID of the service request to update.
     * @param nuevoEstado New status to set (e.g., "APPROVED", "CANCELED").
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SolicitudServicioEntity SS SET SS.estado = ?2 WHERE SS.idSolicitud = ?1")
    void updateEstado(Long idSolicitud, String nuevoEstado);

    /**
     * Delete a service request by its primary key.
     *
     * @param idSolicitud ID of the service request to delete.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SolicitudServicioEntity SS WHERE SS.idSolicitud = ?1")
    void removeById(Long idSolicitud);
}

/*
Summary (Technical Note):
SolicitudServicioDAO is a Spring Data JPA repository for managing SolicitudServicioEntity instances.
It provides:
- Retrieval of service requests by ID, associated service, status, or request date.
- Updating the status of requests with immediate persistence synchronization.
- Deletion of requests by ID.
Transactional annotations optimize read-only queries and ensure proper handling of updates/deletes.
*/
