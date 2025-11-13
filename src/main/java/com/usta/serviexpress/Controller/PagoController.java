package com.usta.serviexpress.Controller;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;
import com.usta.serviexpress.Service.PagoService;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PagoController
 *
 * Purpose:
 * - Provides REST endpoints for managing payment-related operations within ServiExpress.
 * - Handles payment checkout initiation, webhook callbacks from payment gateways, 
 *   and retrieval of payment records.
 *
 * Design:
 * - RESTful API under the base path "/api/pagos".
 * - Uses PagoService to encapsulate business logic and integration with external payment providers.
 *
 * Security & Validation:
 * - Uses Jakarta Validation to validate incoming DTOs for payment initiation.
 * - The webhook endpoint expects a signature header ("X-Signature") for validation of payload authenticity.
 *
 * Response conventions:
 * - Returns JSON responses (via ResponseEntity) suitable for front-end consumption or API integration.
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService; // Service layer handling payment operations and business rules.

    /**
     * Initiates a payment checkout session.
     *
     * Route: POST /api/pagos/checkout
     *
     * @param dto Payment initialization data (validated request body).
     * @return JSON map containing a "checkoutUrl" that the client should redirect to for completing payment.
     *
     * Expected input:
     * - A valid PagoCheckoutInitDTO with fields like amount, service ID, or reference data.
     *
     * Behavior:
     * - Delegates checkout session creation to PagoService, which returns a URL from the payment gateway.
     * - Useful for redirect-based payment workflows (e.g., Stripe Checkout, PayU, MercadoPago).
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> iniciarCheckout(@Valid @RequestBody PagoCheckoutInitDTO dto) {
        String url = pagoService.iniciarCheckout(dto);
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    /**
     * Receives asynchronous webhook notifications from the payment gateway.
     *
     * Route: POST /api/pagos/webhook
     *
     * @param signature Optional header used to verify the integrity and authenticity of the webhook payload.
     * @param payload   Raw JSON payload sent by the payment processor.
     * @return HTTP 200 OK response to acknowledge receipt of the webhook.
     *
     * Behavior:
     * - Delegates verification and processing of the webhook event to PagoService.
     * - The service typically validates the signature, parses the payload, and updates payment status in the database.
     *
     * Notes:
     * - Ensure the endpoint is accessible publicly but secured by signature validation.
     * - Should handle idempotent processing to prevent double updates.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody String payload) {
        pagoService.procesarWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a specific payment record by its ID.
     *
     * Route: GET /api/pagos/{id}
     *
     * @param id Payment identifier.
     * @return PagoEntity serialized as JSON.
     *
     * Behavior:
     * - Calls pagoService.getById() to fetch a payment from the database.
     * - Returns HTTP 200 OK with the payment data.
     *
     * Error handling:
     * - If not found, PagoService may throw an exception handled by a global exception handler.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagoEntity> get(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.getById(id));
    }

    /**
     * Lists all recorded payments.
     *
     * Route: GET /api/pagos
     *
     * @return List of PagoEntity objects as JSON.
     *
     * Behavior:
     * - Retrieves all payments from the persistence layer via PagoService.
     * - Useful for administrative dashboards or debugging.
     */
    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(pagoService.listAll());
    }
}

/*
Summary (Technical Note):
PagoController exposes a REST API for payment operations, including initiating checkout,
handling payment gateway webhooks, and retrieving payment data. It integrates with PagoService
for core logic, maintaining separation between controller and business layers. The controller
validates input DTOs, ensures webhook authenticity, and returns structured JSON responses for 
frontend or third-party integrations. It supports secure and scalable payment handling 
within the ServiExpress system.
*/
