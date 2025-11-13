package com.usta.serviexpress.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CalificacionEntity
 *
 * Purpose:
 * - Represents a rating or review provided by a client for a service or a provider.
 * - Stores key details such as score, optional comment, and timestamp.
 * - Can be linked either to a service (ServicioEntity) or just the provider (UsuarioEntity).
 *
 * Persistence mapping:
 * - Mapped to the "calificaciones" table.
 * - Relationships:
 *   - cliente: mandatory many-to-one to UsuarioEntity representing the client.
 *   - proveedor: mandatory many-to-one to UsuarioEntity representing the provider.
 *   - servicio: optional many-to-one to ServicioEntity; may be null if rating is for provider only.
 *
 * Validation:
 * - puntuacion must be between 1 and 5 inclusive.
 * - comentario is limited to 500 characters.
 *
 * Notes:
 * - LAZY fetch is used for related entities to avoid unnecessary loading.
 * - fecha is automatically set on persist if null.
 */
@Getter
@Setter
@Entity
@Table(name = "calificaciones")
public class CalificacionEntity implements Serializable {

    /**
     * Primary key for the rating.
     * Auto-generated identity column (id_calificacion).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long idCalificacion;

    /**
     * Client who submitted the rating.
     * - Mandatory many-to-one relationship.
     * - LAZY fetch: accessing outside transaction may cause LazyInitializationException.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private UsuarioEntity cliente;

    /**
     * Provider who is being rated.
     * - Mandatory many-to-one relationship.
     * - LAZY fetch: accessing outside transaction may cause LazyInitializationException.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private UsuarioEntity proveedor;

    /**
     * Service associated with this rating.
     * - Optional relationship (can be null if rating is for provider only).
     * - LAZY fetch used for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private ServicioEntity servicio;

    /**
     * Rating score.
     * - Mandatory, between 1 and 5 inclusive.
     */
    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    /**
     * Optional comment associated with the rating.
     * - Maximum 500 characters.
     */
    @Column(name = "comentario", length = 500)
    private String comentario;

    /**
     * Timestamp when the rating was created.
     * - Non-nullable column.
     * - Automatically set to current time on persist if null.
     */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /**
     * JPA lifecycle callback executed before persisting the entity.
     * - Sets 'fecha' to current time if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}

/*
Summary (Technical Note):
CalificacionEntity models a review or rating submitted by a client for a provider and optionally a service.
It persists the score (1-5), an optional comment, the creation timestamp, and links to the client, provider, 
and optionally the service. LAZY fetching is used for relationships to optimize performance. The timestamp is 
automatically set on persist if absent.
*/
