package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.CalificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * CalificacionRepository
 *
 * Purpose:
 * - Repository interface for performing CRUD operations and custom queries on CalificacionEntity.
 * - Provides methods to retrieve ratings by client, by score, and to calculate top providers.
 * - Supports pagination and custom query projections for aggregated statistics.
 *
 * Notes:
 * - Extends JpaRepository for standard JPA operations.
 * - Uses JPQL (@Query) and Spring Data query derivation for flexible querying.
 * - Optional and boolean return types are used for existence checks and single-result queries.
 */
public interface CalificacionRepository extends JpaRepository<CalificacionEntity, Long> {

    /**
     * Retrieves all ratings given by a specific client.
     *
     * @param idCliente the ID of the client (UsuarioEntity)
     * @return list of CalificacionEntity objects given by the client
     */
    @Query("SELECT c FROM CalificacionEntity c WHERE c.cliente.idUsuario = :idCliente")
    List<CalificacionEntity> listByCliente(@Param("idCliente") Long idCliente);

    /**
     * Retrieves all ratings with a specific score, ordered by date descending.
     *
     * @param puntuacion the rating score (1-5)
     * @return list of CalificacionEntity objects with the given score
     */
    List<CalificacionEntity> findByPuntuacionOrderByFechaDesc(Integer puntuacion);

    /**
     * Retrieves aggregated statistics for top providers.
     * - Calculates average rating and total number of ratings per provider.
     * - Filters providers with at least minResenas ratings.
     * - Supports pagination.
     *
     * @param minResenas minimum number of reviews a provider must have
     * @param pageable pageable object for pagination and sorting
     * @return a page of TopProveedorView projections with provider ID, name, average score, and total reviews
     */
    @Query("""
           SELECT p.idUsuario      AS idProveedor,
                  p.nombreUsuario  AS nombreProveedor,
                  AVG(c.puntuacion) AS promedio,
                  COUNT(c.idCalificacion) AS total
           FROM CalificacionEntity c
           JOIN c.proveedor p
           GROUP BY p.idUsuario, p.nombreUsuario
           HAVING COUNT(c.idCalificacion) >= :minResenas
           """)
    Page<TopProveedorView> findTopProveedores(@Param("minResenas") long minResenas, Pageable pageable);

    /**
     * Projection interface for top providers query.
     * - Provides only selected fields instead of full entity.
     */
    interface TopProveedorView {
        Long   getIdProveedor();
        String getNombreProveedor();
        Double getPromedio();
        Long   getTotal();
    }

    /**
     * Retrieves a rating given by a client to a specific provider for a specific service.
     *
     * @param idCliente  client user ID
     * @param idProveedor provider user ID
     * @param idServicio service ID
     * @return optional CalificacionEntity if exists
     */
    Optional<CalificacionEntity> findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
            Long idCliente, Long idProveedor, Long idServicio);

    /**
     * Retrieves a rating given by a client to a provider not associated with a specific service (general rating).
     *
     * @param idCliente  client user ID
     * @param idProveedor provider user ID
     * @return optional CalificacionEntity if exists
     */
    Optional<CalificacionEntity> findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
            Long idCliente, Long idProveedor);

    /**
     * Checks if a rating exists between a client and a provider for a specific service.
     *
     * @param idCliente  client user ID
     * @param idProveedor provider user ID
     * @param idServicio service ID
     * @return true if the rating exists, false otherwise
     */
    boolean existsByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
            Long idCliente, Long idProveedor, Long idServicio);

    /**
     * Checks if a rating exists between a client and a provider without association to a service.
     *
     * @param idCliente  client user ID
     * @param idProveedor provider user ID
     * @return true if the rating exists, false otherwise
     */
    boolean existsByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
            Long idCliente, Long idProveedor);
}

/**
 * Summary:
 * This repository manages CalificacionEntity data access and queries.
 * It supports retrieving ratings by client, by score, checking existence, and calculating top provider statistics.
 * Includes projection interfaces for efficient aggregated queries with pagination.
 */
