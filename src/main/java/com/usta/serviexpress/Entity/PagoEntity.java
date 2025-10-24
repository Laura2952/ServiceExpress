package com.usta.serviexpress.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "PAGOS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pago_solicitud", columnNames = {"id_solicitud"}),
                // opcional pero recomendado para idempotencia del webhook:
                @UniqueConstraint(name = "uk_pago_referencia_ext", columnNames = {"referencia_ext"}),
                // si quieres que el token no se repita:
                @UniqueConstraint(name = "uk_pago_token", columnNames = {"payment_token"})
        }
)
public class PagoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MetodoPago { EFECTIVO, TARJETA, PSE, TRANSFERENCIA, PAYU, STRIPE, OTRO }
    public enum EstadoPago { PENDIENTE, APROBADO, FALLIDO, REEMBOLSADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    @PositiveOrZero
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 20)
    private MetodoPago metodo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    @NotNull
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @NotBlank
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "COP";

    @Size(max = 140)
    @Column(name = "descripcion", length = 140)
    private String descripcion;

    /** ID propio o de la pasarela (Wompi tx id / reference). Único para idempotencia. */
    @Size(max = 120)
    @Column(name = "referencia_ext", length = 120)
    private String referenciaExterna;

    /** Payload crudo del webhook para auditoría. */
    @Lob
    @Column(name = "gateway_payload")
    private String gatewayPayload;

    /** ---- NUEVOS CAMPOS PARA TOKEN + EMAIL + CONFIRMACIÓN ---- */

    /** Token público para iniciar el checkout (no exponer idPago). */
    @Size(max = 64)
    @Column(name = "payment_token", length = 64)
    private String paymentToken;

    /** Expiración del token (ej. ahora + 30 min). */
    @Column(name = "token_expira_en")
    private LocalDateTime tokenExpiraEn;

    /** Correo del comprador para enviar confirmación. */
    @Email
    @Size(max = 120)
    @Column(name = "email_cliente", length = 120)
    private String emailCliente;

    /** Fecha en que quedó APROBADO. */
    @Column(name = "confirmado_en")
    private LocalDateTime confirmadoEn;

    // ---- RELACIONES ----
        @NotNull
        @OneToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "id_solicitud", nullable = false,
                foreignKey = @ForeignKey(name = "fk_pago_solicitud"))
    private SolicitudServicioEntity solicitud;

    // ---- HOOKS ----
    @PrePersist
    public void prePersist() {
        if (fechaPago == null) fechaPago = LocalDateTime.now();
        if (estado == null) estado = EstadoPago.PENDIENTE;
    }
}