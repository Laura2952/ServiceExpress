package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * PagoRepository
 *
 * Purpose:
 * - Repository interface for performing CRUD operations and custom queries on PagoEntity.
 * - Supports searching payments by token, external reference, client email, and payment status.
 * - Enables integration with payment gateways and tracking of payment records.
 *
 * Notes:
 * - Extends JpaRepository for standard JPA operations.
 * - Returns Optional for single-result queries to handle absence of data gracefully.
 * - Supports retrieving multiple payments via List when multiple records may exist.
 */
@Repository
public interface PagoRepository extends JpaRepository<PagoEntity, Long> {

    /**
     * Finds a payment by its unique public token used for initiating checkout.
     *
     * @param paymentToken the public token associated with the payment
     * @return Optional containing the PagoEntity if found, or empty if not
     */
    Optional<PagoEntity> findByPaymentToken(String paymentToken);

    /**
     * Finds a payment by its external reference ID (e.g., from Wompi or other payment gateway).
     *
     * @param referenciaExterna the external reference ID of the payment
     * @return Optional containing the PagoEntity if found, or empty if not
     */
    Optional<PagoEntity> findByReferenciaExterna(String referenciaExterna);

    /**
     * Retrieves all payments associated with a specific client email.
     *
     * @param emailCliente the email of the client who made the payment
     * @return List of PagoEntity objects for the specified email
     */
    List<PagoEntity> findByEmailCliente(String emailCliente);

    /**
     * Retrieves all payments with a specific status.
     *
     * @param estado the payment status (e.g., PENDIENTE, APROBADO, FALLIDO)
     * @return List of PagoEntity objects matching the specified status
     */
    List<PagoEntity> findByEstado(PagoEntity.EstadoPago estado);
}

/**
 * Summary:
 * This repository manages access to PagoEntity records, enabling queries by payment token, external reference,
 * client email, and payment status. It supports both single-result queries using Optional and multi-result queries
 * using List. Useful for payment processing, tracking, and integration with external payment gateways.
 */
