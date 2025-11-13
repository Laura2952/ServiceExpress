package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * SolicitudRepository
 *
 * Purpose:
 * - Repository interface for performing CRUD operations and custom queries on SolicitudServicioEntity.
 * - Provides methods to retrieve service requests by provider, client, state, or combinations.
 * - Includes "deep" queries that fetch related entities to avoid lazy loading issues.
 *
 * Notes:
 * - Extends JpaRepository to leverage standard JPA methods.
 * - Some queries use JPQL with JOIN FETCH to eagerly load related ServicioEntity, provider, and client entities.
 * - Useful for listing service requests with all necessary relationships already loaded for display or processing.
 */
public interface SolicitudRepository extends JpaRepository<SolicitudServicioEntity, Long> {

    // ====== EXISTING SIMPLE QUERIES ======

    /**
     * Retrieves all service requests for a specific provider.
     *
     * @param idProveedor the ID of the provider
     * @return List of SolicitudServicioEntity for the given provider
     */
    List<SolicitudServicioEntity> findByServicio_Proveedor_IdUsuario(Long idProveedor);

    /**
     * Counts all service requests for a specific provider.
     *
     * @param idProveedor the ID of the provider
     * @return total number of service requests for the provider
     */
    long countByServicio_Proveedor_IdUsuario(Long idProveedor);

    /**
     * Finds service requests by their current status.
     *
     * @param estado the status of the request (e.g., PENDIENTE, EN_PROCESO)
     * @return List of SolicitudServicioEntity with the specified status
     */
    List<SolicitudServicioEntity> findByEstado(String estado);

    /**
     * Retrieves all service requests for a specific client.
     *
     * @param cliente the UsuarioEntity representing the client
     * @return List of SolicitudServicioEntity for the client
     */
    List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente);

    /**
     * Custom query to list service requests by provider ID using JPQL.
     *
     * @param idProveedor the ID of the provider
     * @return List of SolicitudServicioEntity associated with the provider
     */
    @Query("""
           SELECT s
           FROM SolicitudServicioEntity s
           WHERE s.servicio.proveedor.idUsuario = :idProveedor
           """)
    List<SolicitudServicioEntity> listarPorProveedor(@Param("idProveedor") Long idProveedor);

    /**
     * Finds service requests by provider entity (alternative to ID-based search).
     *
     * @param proveedor the UsuarioEntity representing the provider
     * @return List of SolicitudServicioEntity for the provider
     */
    List<SolicitudServicioEntity> findByServicio_Proveedor(UsuarioEntity proveedor);

    // ====== NEW QUERIES WITH DEEP RELATIONSHIPS ======

    /**
     * Retrieves all service requests with related ServicioEntity, provider, and client loaded.
     * Results are ordered by request date descending, then by ID descending.
     *
     * @return List of SolicitudServicioEntity with related entities eagerly fetched
     */
    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findAllDeep();

    /**
     * Retrieves all service requests for a specific provider with related entities eagerly loaded.
     *
     * @param proveedor the UsuarioEntity representing the provider
     * @return List of SolicitudServicioEntity for the provider with related entities
     */
    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           where sv.proveedor = :proveedor
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findByProveedorDeep(@Param("proveedor") UsuarioEntity proveedor);

    /**
     * Retrieves all service requests for a specific client with related entities eagerly loaded.
     *
     * @param cliente the UsuarioEntity representing the client
     * @return List of SolicitudServicioEntity for the client with related entities
     */
    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           where s.cliente = :cliente
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findByClienteDeep(@Param("cliente") UsuarioEntity cliente);

}

/**
 * Summary:
 * SolicitudRepository provides methods to query service requests (SolicitudServicioEntity) by provider, client, and state.
 * It includes both simple queries and "deep" queries that eagerly fetch related ServicioEntity, provider, and client entities.
 * These methods support efficient retrieval for displaying request lists, histories, or dashboards while minimizing lazy-loading issues.
 */
