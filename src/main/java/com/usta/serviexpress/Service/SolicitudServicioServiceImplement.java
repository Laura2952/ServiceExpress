package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SolicitudServicioServiceImplement
 *
 * Purpose:
 * - Implementation of the SolicitudServicioService interface.
 * - Provides business logic for managing service requests (SolicitudServicioEntity),
 *   including CRUD operations, filtering by client or provider, and retrieving pending requests.
 *
 * Notes:
 * - Uses Spring's @Transactional for transaction management.
 * - Read-only transactions are applied where no modifications are performed for optimization.
 * - Relies on "deep" repository methods (findAllDeep, findByClienteDeep, findByProveedorDeep)
 *   to eagerly fetch associated entities and avoid lazy loading issues.
 */
@Service
public class SolicitudServicioServiceImplement implements SolicitudServicioService {

    @Autowired
    private SolicitudRepository solicitudRepository; // Repository for CRUD operations on SolicitudServicioEntity

    /**
     * Retrieve all service requests with deep fetching of associated entities.
     *
     * @return List of all SolicitudServicioEntity objects.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findAll() {
        return solicitudRepository.findAllDeep(); // ← important: uses deep fetch
    }

    /**
     * Find a specific service request by its unique ID.
     *
     * @param id The primary key of the service request.
     * @return The corresponding SolicitudServicioEntity, or null if not found.
     */
    @Override
    @Transactional(readOnly = true)
    public SolicitudServicioEntity findById(Long id) {
        return solicitudRepository.findById(id).orElse(null);
    }

    /**
     * Save a new service request or update an existing one.
     *
     * @param solicitud The SolicitudServicioEntity to persist.
     */
    @Override
    @Transactional
    public void save(SolicitudServicioEntity solicitud) {
        solicitudRepository.save(solicitud);
    }

    /**
     * Delete a service request by its ID.
     *
     * @param id The primary key of the SolicitudServicioEntity to delete.
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        solicitudRepository.deleteById(id);
    }

    /**
     * Retrieve all service requests made by a specific client with deep fetching.
     *
     * @param cliente The UsuarioEntity representing the client.
     * @return List of service requests associated with the client.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente) {
        return solicitudRepository.findByClienteDeep(cliente); // ← important: eager fetch
    }

    /**
     * Update a service request only if it already exists in the repository.
     *
     * @param solicitud The SolicitudServicioEntity to update.
     */
    @Override
    @Transactional
    public void actualizarSolicitudServicio(SolicitudServicioEntity solicitud) {
        if (solicitud.getIdSolicitud() != null && solicitudRepository.existsById(solicitud.getIdSolicitud())) {
            solicitudRepository.save(solicitud);
        }
    }

    /**
     * Retrieve all service requests associated with a specific provider by their user ID.
     *
     * @param idProveedor The ID of the provider (UsuarioEntity).
     * @return List of service requests linked to the provider.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByProveedorId(Long idProveedor) {
        return solicitudRepository.findByServicio_Proveedor_IdUsuario(idProveedor);
    }

    /**
     * Retrieve all service requests that are currently pending.
     *
     * @return List of pending SolicitudServicioEntity objects.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> obtenerSolicitudesPendientes() {
        return solicitudRepository.findByEstado("PENDIENTE");
    }

    /**
     * Retrieve all service requests associated with a specific user (client) with deep fetching.
     *
     * @param usuario The UsuarioEntity representing the user.
     * @return List of service requests for the user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> obtenerSolicitudesPorUsuario(UsuarioEntity usuario) {
        return solicitudRepository.findByClienteDeep(usuario); // ← important: eager fetch
    }

    /**
     * Retrieve all service requests associated with a specific provider entity using deep fetching.
     *
     * @param proveedor The UsuarioEntity representing the provider.
     * @return List of service requests linked to the provider.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByProveedor(UsuarioEntity proveedor) {
        return solicitudRepository.findByProveedorDeep(proveedor); // ← important: eager fetch
    }
}

/*
Summary (Technical Note):
SolicitudServicioServiceImplement is the service-layer implementation for managing service requests (SolicitudServicioEntity).
It provides CRUD operations, as well as methods to filter requests by client or provider, and retrieve pending requests.
Deep fetch repository methods are used to prevent lazy loading issues when accessing related entities.
Transactions are managed using Spring's @Transactional annotations, with read-only optimizations applied where applicable.
*/
