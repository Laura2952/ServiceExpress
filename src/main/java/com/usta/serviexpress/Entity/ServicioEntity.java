package com.usta.serviexpress.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ServicioEntity
 *
 * Purpose:
 * - Represents a service offered or requested in the system.
 * - Contains details such as name, description, price, current state, and associations with users.
 * - Can be linked to multiple service requests (solicitudes).
 *
 * Persistence mapping:
 * - Mapped to the "SERVICIO" table.
 * - Uses standard JPA annotations for ID generation, column constraints, and relationships.
 *
 * Validation:
 * - Name, description, price, and state are mandatory.
 * - Price cannot be negative.
 */
@Data
@Entity
@Table(name = "SERVICIO")
public class ServicioEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Primary key for the service */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    /** Name of the service, 1-200 characters, cannot be null */
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    /** Short description of the service, 1-200 characters, cannot be null */
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    /** Price of the service, non-negative, up to 12 digits with 2 decimals */
    @NotNull
    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal precio;

    /**
     * Enumeration for the possible states of a service.
     * - PENDIENTE: Service is pending or newly created
     * - DISPONIBLE: Service is available for requests
     * - OCUPADO: Service is currently in use
     * - ACEPTADA: Service request has been accepted
     * - RECHAZADA: Service request has been rejected
     * - CANCELADA: Service request was canceled
     * - COMPLETADA: Service has been completed
     */
    public enum EstadoServicio {
        PENDIENTE,
        DISPONIBLE,
        OCUPADO,
        ACEPTADA,
        RECHAZADA,
        CANCELADA,
        COMPLETADA
    }

    /** Current state of the service, default is PENDIENTE */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoServicio estado = EstadoServicio.PENDIENTE;

    /**
     * List of service requests (solicitudes) associated with this service.
     * - Lazy loading is used to avoid fetching all requests by default.
     * - @JsonManagedReference prevents circular references during JSON serialization.
     */
    @OneToMany(mappedBy = "servicio", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<SolicitudServicioEntity> solicitudes = new ArrayList<>();

    /**
     * Provider (user) offering this service.
     * - Many services can belong to one provider.
     * - Optional relationship; can be null if service is not yet assigned.
     */
    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private UsuarioEntity proveedor;

    /**
     * Client (user) requesting this service.
     * - Many services can belong to one client.
     * - Optional relationship; can be null if service is only offered and not requested yet.
     */
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private UsuarioEntity cliente;

}

/**
 * Summary:
 * This entity represents services in the application. Each service has a name, description, price, 
 * and current state. Services can be linked to multiple requests and associated with a provider or client.
 * It supports validation for mandatory fields and prevents negative pricing. Enumerated states allow 
 * for easy tracking of the service lifecycle.
 */
