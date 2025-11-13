package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.ServicioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ServicioRepository
 *
 * Purpose:
 * - Repository interface for performing CRUD operations and custom queries on ServicioEntity.
 * - Provides methods to retrieve services by provider, client, status, name, and combinations thereof.
 * - Supports both paginated and non-paginated queries for listing available services.
 *
 * Notes:
 * - Extends JpaRepository to leverage standard JPA methods.
 * - Many queries rely on ServicioEntity.EstadoServicio enum for filtering by service state.
 * - Name searches are case-insensitive using "ContainingIgnoreCase".
 */
public interface ServicioRepository extends JpaRepository<ServicioEntity, Long> {

    /**
     * Finds services for a specific provider with a given state.
     *
     * @param idProveedor the ID of the provider
     * @param estado the state of the service (enum EstadoServicio)
     * @return List of ServicioEntity objects matching the provider and state
     */
    List<ServicioEntity> findByProveedor_IdUsuarioAndEstado(Long idProveedor,
                                                            ServicioEntity.EstadoServicio estado);

    /**
     * Retrieves all services for a specific provider (no state filter).
     *
     * @param idProveedor the ID of the provider
     * @return List of ServicioEntity objects belonging to the provider
     */
    List<ServicioEntity> findByProveedor_IdUsuario(Long idProveedor);

    /**
     * Retrieves all services requested by a specific client.
     *
     * @param cliente the UsuarioEntity representing the client
     * @return List of ServicioEntity objects associated with the client
     */
    List<ServicioEntity> findByCliente(com.usta.serviexpress.Entity.UsuarioEntity cliente);

    /**
     * Searches for services containing the specified name (case-insensitive, non-paginated).
     *
     * @param nombre the search keyword for service name
     * @return List of ServicioEntity objects matching the name
     */
    List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Retrieves services by their state (non-paginated).
     *
     * @param estado the state of the service (enum EstadoServicio)
     * @return List of ServicioEntity objects with the specified state
     */
    List<ServicioEntity> findByEstado(ServicioEntity.EstadoServicio estado);

    /**
     * Retrieves services by their state with pagination support.
     *
     * @param estado the state of the service (enum EstadoServicio)
     * @param pageable Pageable object for pagination
     * @return Page of ServicioEntity objects matching the state
     */
    Page<ServicioEntity> findByEstado(ServicioEntity.EstadoServicio estado, Pageable pageable);

    /**
     * Searches for available services by state and name (case-insensitive, non-paginated).
     *
     * @param estado the state of the service (enum EstadoServicio)
     * @param nombre the keyword to search in service names
     * @return List of ServicioEntity objects matching the state and name
     */
    List<ServicioEntity> findByEstadoAndNombreContainingIgnoreCase(ServicioEntity.EstadoServicio estado, String nombre);

    /**
     * Counts the total number of services associated with a specific provider.
     *
     * @param idProveedor the ID of the provider
     * @return the number of services belonging to the provider
     */
    long countByProveedor_IdUsuario(Long idProveedor);

    /**
     * Searches for services by provider ID and name (case-insensitive).
     *
     * @param idUsuario the ID of the provider
     * @param nombre the keyword to search in service names
     * @return List of ServicioEntity objects matching the provider and name
     */
    List<ServicioEntity> findByProveedor_IdUsuarioAndNombreContainingIgnoreCase(Long idUsuario, String nombre);

}

/**
 * Summary:
 * ServicioRepository provides methods to query ServicioEntity by provider, client, state, and name.
 * It supports paginated and non-paginated results and enables filtering using both enum states and case-insensitive name searches.
 * Common use cases include listing available services, searching by keyword, retrieving provider history, and counting services.
 */
