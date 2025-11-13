package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;

import java.util.List;

/**
 * SolicitudServicioService
 *
 * Interface defining the service layer for managing SolicitudServicioEntity objects.
 * Provides CRUD operations, filtering by client, provider, and status.
 * Intended to separate business logic from repository access.
 */
public interface SolicitudServicioService {

    /**
     * Retrieve all service requests.
     * @return List of all SolicitudServicioEntity objects
     */
    List<SolicitudServicioEntity> findAll();

    /**
     * Find a service request by its ID.
     * @param id ID of the service request
     * @return SolicitudServicioEntity if found; null otherwise
     */
    SolicitudServicioEntity findById(Long id);

    /**
     * Save a new service request or update an existing one.
     * @param solicitud SolicitudServicioEntity to persist
     */
    void save(SolicitudServicioEntity solicitud);

    /**
     * Delete a service request by its ID.
     * @param id ID of the service request to delete
     */
    void deleteById(Long id);

    /**
     * Retrieve all service requests made by a specific client.
     * @param cliente UsuarioEntity representing the client
     * @return List of SolicitudServicioEntity objects for the client
     */
    List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente);

    /**
     * Update the details or status of an existing service request.
     * @param solicitud SolicitudServicioEntity with updated information
     */
    void actualizarSolicitudServicio(SolicitudServicioEntity solicitud);

    /**
     * Retrieve all service requests assigned to a specific provider using their ID.
     * @param idProveedor ID of the provider
     * @return List of SolicitudServicioEntity objects for the provider
     */
    List<SolicitudServicioEntity> findByProveedorId(Long idProveedor);

    /**
     * Retrieve all pending service requests (not yet approved or rejected).
     * @return List of pending SolicitudServicioEntity objects
     */
    List<SolicitudServicioEntity> obtenerSolicitudesPendientes();

    /**
     * Retrieve all service requests associated with a specific user (as client or provider).
     * @param usuario UsuarioEntity representing the user
     * @return List of SolicitudServicioEntity objects related to the user
     */
    List<SolicitudServicioEntity> obtenerSolicitudesPorUsuario(UsuarioEntity usuario);

    /**
     * Retrieve all service requests assigned to a specific provider.
     * @param proveedor UsuarioEntity representing the provider
     * @return List of SolicitudServicioEntity objects assigned to the provider
     */
    List<SolicitudServicioEntity> findByProveedor(UsuarioEntity proveedor);
}
