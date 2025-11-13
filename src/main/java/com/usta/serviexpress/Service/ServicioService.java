package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * ServicioService
 *
 * Purpose:
 * - Service interface defining operations for managing ServicioEntity objects.
 * - Supports CRUD operations, searching, filtering by status or provider, and pagination.
 *
 * Notes:
 * - Methods returning List fetch all matching entities in memory.
 * - Methods returning Page support pagination for large datasets.
 * - Many methods are filtered by 'disponible' status, provider, or client.
 */
public interface ServicioService {

    /**
     * Save or update a ServicioEntity.
     * @param servicio ServicioEntity to persist
     * @return Persisted ServicioEntity with any generated fields populated (e.g., id)
     */
    ServicioEntity save(ServicioEntity servicio);

    /**
     * Find a ServicioEntity by its ID.
     * @param idServicio ID of the service
     * @return ServicioEntity if found; may throw exception if not found depending on implementation
     */
    ServicioEntity findById(Long idServicio);

    /**
     * Find pending services for a specific provider (status = PENDING).
     * @param idUsuario ID of the provider
     * @return List of pending ServicioEntity objects
     */
    List<ServicioEntity> findPendientesByProveedor(Long idUsuario);

    /**
     * Find historical services for a provider (completed or past services).
     * @param idUsuario ID of the provider
     * @return List of historical ServicioEntity objects
     */
    List<ServicioEntity> findHistorialByProveedor(Long idUsuario);

    /**
     * Find all services associated with a specific client.
     * @param cliente UsuarioEntity representing the client
     * @return List of ServicioEntity objects for the client
     */
    List<ServicioEntity> findByCliente(UsuarioEntity cliente);

    /**
     * Retrieve all services without any filter.
     * @return List of all ServicioEntity objects
     */
    List<ServicioEntity> findAll();

    /**
     * Delete a service by its ID.
     * @param id ID of the service to delete
     */
    void deleteById(Long id);

    /**
     * Search services by name (case-insensitive, partial match).
     * @param nombre Name string to search for
     * @return List of matching ServicioEntity objects
     */
    List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre);

    // ------------------------
    // Available services (non-paginated)
    // ------------------------

    /**
     * Retrieve only services with status = DISPONIBLE.
     * @return List of available services
     */
    List<ServicioEntity> findDisponibles();

    /**
     * Search available services by name (case-insensitive).
     * @param nombre Name string to search for
     * @return List of matching available services
     */
    List<ServicioEntity> findDisponiblesPorNombre(String nombre);

    // ------------------------
    // Pagination support
    // ------------------------

    /**
     * List all services with pagination.
     * @param pageable Pageable object with page number, size, and sort
     * @return Page of ServicioEntity objects
     */
    Page<ServicioEntity> listar(Pageable pageable);

    /**
     * List only DISPONIBLE services with pagination.
     * @param pageable Pageable object with page number, size, and sort
     * @return Page of available ServicioEntity objects
     */
    Page<ServicioEntity> listarDisponibles(Pageable pageable);

    // ------------------------
    // Provider-specific queries
    // ------------------------

    /**
     * Find all services offered by a specific provider.
     * @param idUsuario ID of the provider
     * @return List of ServicioEntity objects for the provider
     */
    List<ServicioEntity> findByProveedor(Long idUsuario);

    /**
     * Search provider's services by name (case-insensitive).
     * @param idUsuario ID of the provider
     * @param nombre Name string to search for
     * @return List of matching ServicioEntity objects for the provider
     */
    List<ServicioEntity> findByProveedorAndNombreContainingIgnoreCase(Long idUsuario, String nombre);
}

/*
Summary (Technical Note):
ServicioService defines the main business operations for managing ServicioEntity objects.
It provides CRUD operations, searches by name, filtering by status (e.g., DISPONIBLE, PENDING),
and queries specific to clients or providers. Supports both non-paginated and paginated
retrieval to handle large datasets efficiently. Methods are designed to be implemented
by a Service class interacting with the database via repositories or DAO layers.
*/
