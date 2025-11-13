package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ServicioServiceImplement
 *
 * Purpose:
 * - Concrete implementation of ServicioService interface.
 * - Handles business logic and data access for ServicioEntity objects using ServicioRepository.
 *
 * Notes:
 * - Provides CRUD operations, filtering by provider, client, name, and service status.
 * - Supports both paginated and non-paginated retrieval.
 * - Non-paginated methods are suitable for small datasets; use paginated methods for large datasets.
 */
@Service
@RequiredArgsConstructor
public class ServicioServiceImplement implements ServicioService {

    private final ServicioRepository servicioRepository;

    /**
     * Save or update a ServicioEntity.
     * @param servicio ServicioEntity to persist
     * @return Persisted ServicioEntity
     */
    @Override
    public ServicioEntity save(ServicioEntity servicio) {
        servicioRepository.save(servicio);
        return servicio;
    }

    /**
     * Find a ServicioEntity by its ID.
     * @param idServicio ID of the service
     * @return ServicioEntity if found; null otherwise
     */
    @Override
    public ServicioEntity findById(Long idServicio) {
        return servicioRepository.findById(idServicio).orElse(null);
    }

    /**
     * Retrieve pending services for a specific provider.
     * @param idUsuario ID of the provider
     * @return List of pending ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findPendientesByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuarioAndEstado(
                idUsuario,
                ServicioEntity.EstadoServicio.PENDIENTE
        );
    }

    /**
     * Retrieve all historical services for a provider.
     * @param idUsuario ID of the provider
     * @return List of ServicioEntity objects associated with the provider
     */
    @Override
    public List<ServicioEntity> findHistorialByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuario(idUsuario);
    }

    /**
     * Retrieve all services requested by a specific client.
     * @param cliente UsuarioEntity representing the client
     * @return List of ServicioEntity objects for the client
     */
    @Override
    public List<ServicioEntity> findByCliente(UsuarioEntity cliente) {
        return servicioRepository.findByCliente(cliente);
    }

    /**
     * Retrieve all services.
     * @return List of all ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findAll() {
        return servicioRepository.findAll();
    }

    /**
     * Delete a service by its ID.
     * @param id ID of the service to delete
     */
    @Override
    public void deleteById(Long id) {
        servicioRepository.deleteById(id);
    }

    /**
     * Search services by name (case-insensitive).
     * @param nombre Name string to search for
     * @return List of matching ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre) {
        return servicioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== Non-paginated available services =====

    /**
     * Retrieve all available services (status = DISPONIBLE).
     * @return List of available ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findDisponibles() {
        return servicioRepository.findByEstado(ServicioEntity.EstadoServicio.DISPONIBLE);
    }

    /**
     * Search available services by name (case-insensitive).
     * @param nombre Name string to search for
     * @return List of matching available services
     */
    @Override
    public List<ServicioEntity> findDisponiblesPorNombre(String nombre) {
        return servicioRepository.findByEstadoAndNombreContainingIgnoreCase(
                ServicioEntity.EstadoServicio.DISPONIBLE, nombre
        );
    }

    // ===== Paginated methods =====

    /**
     * List all services with pagination.
     * @param pageable Pageable object with page number, size, and sorting
     * @return Page of ServicioEntity objects
     */
    @Override
    public Page<ServicioEntity> listar(Pageable pageable) {
        return servicioRepository.findAll(pageable);
    }

    /**
     * List available services with pagination.
     * @param pageable Pageable object with page number, size, and sorting
     * @return Page of available ServicioEntity objects
     */
    @Override
    public Page<ServicioEntity> listarDisponibles(Pageable pageable) {
        return servicioRepository.findByEstado(ServicioEntity.EstadoServicio.DISPONIBLE, pageable);
    }

    // ===== Provider-specific queries =====

    /**
     * Retrieve all services offered by a specific provider.
     * @param idUsuario ID of the provider
     * @return List of ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuario(idUsuario);
    }

    /**
     * Search provider's services by name (case-insensitive).
     * @param idUsuario ID of the provider
     * @param nombre Name string to search for
     * @return List of matching ServicioEntity objects
     */
    @Override
    public List<ServicioEntity> findByProveedorAndNombreContainingIgnoreCase(Long idUsuario, String nombre) {
        return servicioRepository.findByProveedor_IdUsuarioAndNombreContainingIgnoreCase(idUsuario, nombre);
    }

}

/*
Summary (Technical Note):
ServicioServiceImplement provides the concrete implementation of the ServicioService interface.
It manages CRUD operations for ServicioEntity objects, supports filtering by provider, client,
service name, and status (DISPONIBLE, PENDIENTE). Also includes both paginated and non-paginated
retrieval methods to efficiently handle large datasets. All database access is delegated to
ServicioRepository.
*/
