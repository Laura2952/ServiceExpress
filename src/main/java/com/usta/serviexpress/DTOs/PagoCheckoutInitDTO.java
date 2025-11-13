package com.usta.serviexpress.DTOs;

import com.usta.serviexpress.Entity.PagoEntity;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * PagoCheckoutInitDTO
 *
 * Purpose:
 * - Data Transfer Object (DTO) used to initiate a payment checkout process.
 * - Encapsulates all required information for creating a payment request, including amount,
 *   payment method, URLs for front-end redirection and webhook notifications, and customer email.
 *
 * Validation:
 * - Ensures required fields are present and adhere to constraints such as positive amounts,
 *   valid email format, and maximum length restrictions.
 *
 * Important considerations:
 * - 'monto' uses BigDecimal for precise financial calculations.
 * - 'returnUrl' and 'notifyUrl' must be valid URLs handled by the frontend and backend respectively.
 * - 'metodo' is an enum indicating the chosen payment method (e.g., TARJETA, PSE).
 */
@Data
public class PagoCheckoutInitDTO {

    /**
     * Identifier of the payment request or associated service order.
     * - Mandatory field.
     */
    @NotNull
    private Long idSolicitud;

    /**
     * Payment amount.
     * - Mandatory, must be zero or positive.
     * - Maximum 12 integer digits and 2 decimal places.
     */
    @NotNull
    @PositiveOrZero
    @Digits(integer = 12, fraction = 2)
    private BigDecimal monto;

    /**
     * Payment method to be used for the transaction.
     * - Mandatory field.
     * - Enum defined in PagoEntity (e.g., TARJETA, PSE).
     */
    @NotNull
    private PagoEntity.MetodoPago metodo;

    /**
     * Optional description of the payment.
     * - Maximum length: 140 characters.
     */
    @Size(max = 140)
    private String descripcion;

    /**
     * URL to which the user will be redirected after completing the payment.
     * - Mandatory, should be handled by the frontend application.
     */
    @NotBlank
    private String returnUrl;

    /**
     * URL to receive asynchronous notifications (webhooks) from the payment provider.
     * - Mandatory.
     */
    @NotBlank
    private String notifyUrl;

    /**
     * Customer email address associated with the payment.
     * - Mandatory and must be a valid email format.
     */
    @Email
    @NotBlank
    private String email;
}

/*
Summary (Technical Note):
PagoCheckoutInitDTO is a data transfer object used to initiate a payment checkout. It validates
mandatory fields including the associated request ID, payment amount, chosen payment method, 
URLs for frontend redirection and backend notifications, and customer email. Amount is restricted
to positive values with up to 12 integer digits and 2 decimals. Optional description is limited
to 140 characters.
*/
