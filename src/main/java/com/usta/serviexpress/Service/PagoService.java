package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;

import java.util.List;

/**
 * PagoService
 *
 * Interface defining the operations for handling payments (Pagos) in the system.
 * Includes methods for initiating checkouts, processing webhooks, updating payment status,
 * and retrieving payment records.
 */
public interface PagoService {

    /**
     * Initiates a checkout process with a payment gateway (e.g., Wompi).
     *
     * @param dto DTO containing payment details:
     *            - monto
     *            - moneda
     *            - emailCliente
     *            - descripcion
     * @return the URL where the client can complete the payment
     */
    String iniciarCheckout(PagoCheckoutInitDTO dto);

    /**
     * Processes incoming webhook events from the payment gateway.
     * Usually used to update payment status asynchronously.
     *
     * @param signature the signature sent by the payment gateway for verification
     * @param payload   the raw JSON payload of the webhook
     */
    void procesarWebhook(String signature, String payload);

    /**
     * Updates the payment status for a specific payment reference.
     *
     * @param ref     external reference ID of the payment
     * @param estado  new payment status (e.g., PENDIENTE, APROBADO)
     * @param payload optional raw payload from the payment provider
     * @return the updated PagoEntity
     */
    PagoEntity actualizarEstadoPorReferencia(String ref, PagoEntity.EstadoPago estado, String payload);

    /**
     * Retrieves a payment by its internal ID.
     *
     * @param id the internal ID of the payment
     * @return the PagoEntity if found, or null/exception depending on implementation
     */
    PagoEntity getById(Long id);

    /**
     * Returns a list of all payment records in the system.
     *
     * @return list of PagoEntity
     */
    List<PagoEntity> listAll();
}
