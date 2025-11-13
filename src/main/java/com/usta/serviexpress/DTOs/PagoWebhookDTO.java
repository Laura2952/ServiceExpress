package com.usta.serviexpress.DTOs;

import lombok.Data;

/**
 * PagoWebhookDTO
 *
 * Purpose:
 * - Generic data transfer object (DTO) for receiving webhook notifications from a payment gateway.
 * - Captures the essential information about the payment status sent by the gateway.
 *
 * Important considerations:
 * - Structure is generic; adapt field names/types to match the specific gateway you are integrating.
 * - 'rawJson' preserves the original payload if you want to handle it manually instead of relying on automatic deserialization.
 */
@Data
public class PagoWebhookDTO {

    /**
     * External reference or transaction ID assigned by the payment gateway.
     * - Used to correlate webhook events with your internal payment records.
     */
    private String referenciaExterna;

    /**
     * Current status of the payment.
     * - Typical values: APPROVED, DECLINED, PENDING, REFUNDED.
     */
    private String estado;

    /**
     * Optional internal payment ID.
     * - Can be used if your system sends its own identifier with the transaction.
     */
    private Long idPago;

    /**
     * Raw JSON payload received from the gateway webhook.
     * - Useful if you do not want to map the payload directly via @RequestBody.
     */
    private String rawJson;
}

/*
Summary (Technical Note):
PagoWebhookDTO is a generic DTO designed to capture webhook notifications from a payment gateway.
It includes the gateway's transaction reference, the payment status, an optional internal payment ID,
and the raw JSON payload for flexibility in processing or logging. Adapt field names and types as needed
for specific gateway integrations.
*/
