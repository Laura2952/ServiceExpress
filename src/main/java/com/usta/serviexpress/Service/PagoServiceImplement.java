package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;
import com.usta.serviexpress.Repository.PagoRepository;
import com.usta.serviexpress.Repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PagoServiceImplement
 *
 * Purpose:
 * - Provides a service-agnostic implementation for handling payment operations.
 * - Supports initiating checkouts, processing webhooks, updating payment status, and retrieving payments.
 * - Can be extended to integrate with a real Payment Provider (e.g., Wompi, PayU).
 *
 * Notes:
 * - Current implementation uses a placeholder/mock checkout URL.
 * - Webhook processing is minimal; JSON parsing for reference and status must be implemented in production.
 * - Transactional annotations ensure database consistency.
 */
@Service
@RequiredArgsConstructor
public class PagoServiceImplement implements PagoService {

    // Repositories for accessing payment and request (solicitud) data
    private final PagoRepository pagoRepo;
    private final SolicitudRepository solicitudRepo;

    // Uncomment and inject your real PaymentProvider implementation if available
    // private final PaymentProvider provider;

    /**
     * Initiates a checkout process for a given payment request.
     *
     * @param dto Data Transfer Object containing checkout initialization parameters:
     *            - idSolicitud: ID of the related request
     *            - monto: payment amount
     *            - metodo: payment method
     *            - descripcion: description of the payment
     * @return URL for the checkout page (currently a placeholder)
     *
     * Notes:
     * - The payment is persisted with status PENDING before returning the checkout URL.
     * - For a real provider, replace the placeholder URL with the provider-generated checkout URL.
     */
    @Override
    @Transactional
    public String iniciarCheckout(PagoCheckoutInitDTO dto) {
        // Fetch associated solicitud, or throw if not found
        var solicitud = solicitudRepo.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        // Create and persist new payment entity
        PagoEntity pago = new PagoEntity();
        pago.setSolicitud(solicitud);
        pago.setMonto(dto.getMonto());
        pago.setMetodo(dto.getMetodo());
        pago.setEstado(PagoEntity.EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMoneda("COP");
        pago.setDescripcion(dto.getDescripcion());
        pago = pagoRepo.save(pago);

        // Placeholder for integration with a real payment provider
        // Example:
        // CreateCheckoutRequest req = ...
        // var res = provider.createCheckout(req);
        // pago.setReferenciaExterna(res.getTransactionOrSessionId());
        // pagoRepo.save(pago);
        // return res.getCheckoutUrl();

        // Return temporary mock URL for development/testing purposes
        return "/pagos/mock-checkout/" + pago.getIdPago();
    }

    /**
     * Processes incoming webhook payloads from a payment provider.
     *
     * @param signature Signature sent by the payment provider (unused in this mock)
     * @param payload   Raw webhook JSON payload containing payment reference and status
     *
     * Notes:
     * - Currently uses helper methods to extract a dummy reference and status.
     * - Maps provider status to PagoEntity.EstadoPago enum.
     * - Calls actualizarEstadoPorReferencia to update payment state in the database.
     */
    @Override
    @Transactional
    public void procesarWebhook(String signature, String payload) {
        String ref = extraerRef(payload);     // Extracts transaction reference (dummy for now)
        String estado = extraerEstado(payload); // Extracts status string (dummy)

        // Map string status to enum value
        PagoEntity.EstadoPago nuevoEstado = switch (estado.toUpperCase()) {
            case "APPROVED", "APROBADO" -> PagoEntity.EstadoPago.APROBADO;
            case "DECLINED", "FALLIDO" -> PagoEntity.EstadoPago.FALLIDO;
            case "REFUNDED", "REEMBOLSADO" -> PagoEntity.EstadoPago.REEMBOLSADO;
            default -> PagoEntity.EstadoPago.PENDIENTE;
        };

        // Update payment state in database
        actualizarEstadoPorReferencia(ref, nuevoEstado, payload);
    }

    /**
     * Updates the payment status based on an external reference.
     *
     * @param ref     External transaction reference from the payment provider
     * @param estado  New payment state to apply (enum PagoEntity.EstadoPago)
     * @param payload Raw webhook JSON payload for audit/tracking
     * @return Updated PagoEntity after saving to database
     *
     * Notes:
     * - Throws IllegalArgumentException if no payment matches the reference.
     */
    @Override
    @Transactional
    public PagoEntity actualizarEstadoPorReferencia(String ref, PagoEntity.EstadoPago estado, String payload) {
        var pago = pagoRepo.findByReferenciaExterna(ref)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado por referencia: " + ref));
        pago.setEstado(estado);
        pago.setGatewayPayload(payload);
        return pagoRepo.save(pago);
    }

    /**
     * Retrieves a payment by its database ID.
     *
     * @param id Payment entity ID
     * @return PagoEntity corresponding to the given ID
     * @throws NoSuchElementException if the payment does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public PagoEntity getById(Long id) {
        return pagoRepo.findById(id).orElseThrow();
    }

    /**
     * Returns a list of all payments in the system.
     *
     * @return List of PagoEntity
     */
    @Override
    @Transactional(readOnly = true)
    public List<PagoEntity> listAll() {
        return pagoRepo.findAll();
    }

    // --- Helper methods for demo purposes ---

    /**
     * Extracts transaction reference from webhook payload.
     * Currently returns a placeholder value.
     *
     * @param payload Raw webhook payload
     * @return Transaction reference string
     */
    private String extraerRef(String payload) {
        return "tx_demo_ref"; // TODO: implement real JSON parsing
    }

    /**
     * Extracts payment status from webhook payload.
     * Currently returns a placeholder value.
     *
     * @param payload Raw webhook payload
     * @return Status string (e.g., "APPROVED")
     */
    private String extraerEstado(String payload) {
        return "APPROVED"; // TODO: implement real JSON parsing
    }
}

/*
Summary (Technical Note):
PagoServiceImplement provides an agnostic payment service for managing payment lifecycle:
- Initiates checkouts, persisting payments with PENDING status.
- Processes webhook notifications from payment providers to update payment state.
- Allows retrieval of payments by ID or listing all payments.
- Current implementation uses mock URLs and placeholder webhook parsing; real provider integration
  should replace placeholders with actual checkout URLs and JSON parsing logic.
Transactional annotations ensure database consistency during updates.
*/
