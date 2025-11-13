package com.usta.serviexpress.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PagoEntity
 *
 * Purpose:
 * - Represents a payment made for a service request (SolicitudServicioEntity).
 * - Stores payment details such as amount, method, currency, status, and optional metadata.
 * - Includes fields to support idempotency and webhook handling.
 *
 * Persistence mapping:
 * - Mapped to the "PAGOS" table.
 * - Unique constraints:
 *   - id_solicitud: each service request can have at most one payment.
 *   - referencia_ext: unique external reference from payment gateway (supports idempotency).
 *   - payment_token: ensures no duplicate checkout tokens.
 *
 * Validation:
 * - monto must be non-negative with up to 12 integer digits and 2 fraction digits.
 * - metodo and estado are enums, stored as strings.
 * - emailCliente validated as a proper email if provided.
 *
 * Hooks:
 * - @PrePersist sets fechaPago to now if null and estado to PENDIENTE if null.
 */
@Data
@Entity
@Table(
        name = "PAGOS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pago_solicitud", columnNames = {"id_solicitud"}),
                @UniqueConstraint(name = "uk_pago_referencia_ext", columnNames = {"referencia_ext"}),
                @UniqueConstraint(name = "uk_pago_token", columnNames = {"payment_token"})
        }
)
public class PagoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Enum for supported payment methods */
    public enum MetodoPago { EFECTIVO, TARJETA, PSE, TRANSFERENCIA, PAYU, STRIPE, OTRO }

    /** Enum for current payment status */
    public enum EstadoPago { PENDIENTE, APROBADO, FALLIDO, REEMBOLSADO }

    /** Primary key for the payment */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    /** Amount to be paid; must be >= 0 */
    @NotNull
    @Digits(integer = 12, fraction = 2)
    @PositiveOrZero
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Payment method (enum) */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 20)
    private MetodoPago metodo;

    /** Payment status (enum) */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    /** Timestamp when the payment was made */
    @NotNull
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    /** Currency code (ISO 4217, default COP) */
    @NotBlank
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "COP";

    /** Optional description of the payment (max 140 chars) */
    @Size(max = 140)
    @Column(name = "descripcion", length = 140)
    private String descripcion;

    /** External reference ID from the payment gateway (used for idempotency) */
    @Size(max = 120)
    @Column(name = "referencia_ext", length = 120)
    private String referenciaExterna;

    /** Raw webhook payload from gateway (audit purposes) */
    @Lob
    @Column(name = "gateway_payload")
    private String gatewayPayload;

    /** Public checkout token to initiate payment (not the internal idPago) */
    @Size(max = 64)
    @Column(name = "payment_token", length = 64)
    private String paymentToken;

    /** Expiration timestamp of the checkout token */
    @Column(name = "token_expira_en")
    private LocalDateTime tokenExpiraEn;

    /** Email of the payer (used to send confirmation) */
    @Email
    @Size(max = 120)
    @Column(name = "email_cliente", length = 120)
    private String emailCliente;

    /** Timestamp when the payment was confirmed */
    @Column(name = "confirmado_en")
    private LocalDateTime confirmadoEn;

    // ---- RELATIONSHIPS ----
    /**
     * Associated service request
     * - One-to-one mandatory relation
     * - Lazy loading for performance
     */
    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pago_solicitud"))
    private SolicitudServicioEntity solicitud;

    // ---- LIFECYCLE HOOKS ----
    /**
     * JPA lifecycle callback executed before persisting the entity.
     * - Sets fechaPago to now if null
     * - Sets estado to PENDIENTE if null
     */
    @PrePersist
    public void prePersist() {
        if (fechaPago == null) fechaPago = LocalDateTime.now();
        if (estado == null) estado = EstadoPago.PENDIENTE;
    }
}
