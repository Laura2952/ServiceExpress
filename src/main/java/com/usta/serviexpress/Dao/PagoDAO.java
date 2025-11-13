package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * PagoDAO
 *
 * Purpose:
 * - Data Access Object (DAO) for performing CRUD operations and custom queries on PagoEntity.
 * - Provides methods to retrieve payments by different identifiers and update payment status or gateway-related fields.
 *
 * Type parameters:
 * - PagoEntity: The entity type managed by this repository.
 * - Long: Type of the entity's primary key.
 *
 * Transactional and modifying notes:
 * - Read-only queries are annotated with @Transactional(readOnly = true) to optimize performance.
 * - Update operations use @Modifying and @Transactional to handle persistence context flushing and automatic clearing.
 * - 'clearAutomatically' and 'flushAutomatically' ensure the persistence context is synchronized after updates.
 */
public interface PagoDAO extends JpaRepository<PagoEntity, Long> {

    /**
     * Retrieve a PagoEntity by its primary key (idPago).
     *
     * @param idPago The payment ID.
     * @return The PagoEntity matching the given ID, or null if not found.
     */
    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.idPago = ?1")
    PagoEntity viewDetail(Long idPago);

    /**
     * Retrieve a PagoEntity by the external reference provided by the payment gateway.
     *
     * @param referenciaExterna External transaction reference.
     * @return The matching PagoEntity, or null if not found.
     */
    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.referenciaExterna = ?1")
    PagoEntity findByReferenciaExterna(String referenciaExterna);

    /**
     * Retrieve a PagoEntity associated with a specific service request ID.
     *
     * @param idSolicitud Service request ID.
     * @return The corresponding PagoEntity, or null if not found.
     */
    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.solicitud.idSolicitud = ?1")
    PagoEntity findBySolicitudId(Long idSolicitud);

    /**
     * Update the status (estado) of a payment by its primary key.
     *
     * @param idPago Payment ID.
     * @param nuevoEstado New payment status.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.estado = ?2 WHERE P.idPago = ?1")
    void changeEstado(Long idPago, PagoEntity.EstadoPago nuevoEstado);

    /**
     * Update the status and raw gateway payload of a payment using the external reference.
     *
     * @param referenciaExterna External transaction reference.
     * @param nuevoEstado New payment status.
     * @param rawPayload Raw JSON payload from the gateway.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.estado = ?2, P.gatewayPayload = ?3 WHERE P.referenciaExterna = ?1")
    void changeEstadoByReferencia(String referenciaExterna, PagoEntity.EstadoPago nuevoEstado, String rawPayload);

    /**
     * Set the external reference (referenciaExterna) for a payment by its ID.
     *
     * @param idPago Payment ID.
     * @param referenciaExterna External reference from the payment gateway.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.referenciaExterna = ?2 WHERE P.idPago = ?1")
    void setReferenciaExterna(Long idPago, String referenciaExterna);

    /**
     * Update the raw gateway payload of a payment by its ID.
     *
     * @param idPago Payment ID.
     * @param rawPayload Raw JSON payload from the gateway.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.gatewayPayload = ?2 WHERE P.idPago = ?1")
    void setGatewayPayload(Long idPago, String rawPayload);
}

/*
Summary (Technical Note):
PagoDAO is a Spring Data JPA repository for managing PagoEntity instances. It provides methods to:
- Retrieve payments by internal ID, external gateway reference, or associated service request ID.
- Update payment status and gateway payloads, ensuring persistence context synchronization.
- Set or update external references and raw gateway payloads. 
Transactional annotations optimize read-only queries and manage updates correctly.
*/
