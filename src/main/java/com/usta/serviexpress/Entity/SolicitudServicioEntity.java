package com.usta.serviexpress.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * SolicitudServicioEntity
 *
 * Purpose:
 * - Represents a service request made by a client for a particular service.
 * - Contains details such as request date, state, delivery information, and optional estimated date.
 * - Links to a service, the requesting client, and an optional payment entity.
 *
 * Persistence mapping:
 * - Mapped to the "SOLICITUD_SERVICIO" table.
 * - Standard JPA annotations for ID generation, column constraints, and relationships.
 *
 * Notes:
 * - Estado (state) is a free-text field but expected to use defined states like:
 *   PENDIENTE, PAGO_EN_PROCESO, PAGO_ACEPTADO, EN_PROCESO, FINALIZADO, etc.
 * - fechaEstimada and pago are optional; may be null.
 */
@Entity
@Table(name = "SOLICITUD_SERVICIO")
public class SolicitudServicioEntity implements Serializable {

    /** Primary key for the service request */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    /** Date when the service request was created */
    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    /** Current state of the service request (e.g., PENDING, PAYMENT_PROCESSING, COMPLETED) */
    @Column(name = "estado", length = 40)
    private String estado;

    // ===== ADDITIONAL DETAILS =====
    /** Client-provided specifications or details for the service request */
    @Size(max = 500)
    @Column(name = "detalles", length = 500)
    private String detalles;

    /** Delivery address confirmation for the service request */
    @Size(max = 180)
    @Column(name = "direccion_entrega", length = 180)
    private String direccionEntrega;

    /** Optional estimated date for service completion or delivery */
    @Column(name = "fecha_estimada")
    private LocalDate fechaEstimada;

    // ===== RELATIONSHIPS =====
    /**
     * Service associated with this request.
     * - Many requests can belong to one service.
     * - Lazy fetch is used to optimize performance.
     * - @JsonBackReference avoids circular references during JSON serialization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    @JsonBackReference
    private ServicioEntity servicio;

    /**
     * Client (user) who made the service request.
     * - Many requests can belong to one client.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private UsuarioEntity cliente;

    /**
     * Payment associated with this service request.
     * - Optional relationship; may be null if payment has not been made yet.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago")
    private PagoEntity pago;

    // ===== GETTERS AND SETTERS =====
    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDate fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public LocalDate getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDate fechaEstimada) { this.fechaEstimada = fechaEstimada; }

    public ServicioEntity getServicio() { return servicio; }
    public void setServicio(ServicioEntity servicio) { this.servicio = servicio; }

    public UsuarioEntity getCliente() { return cliente; }
    public void setCliente(UsuarioEntity cliente) { this.cliente = cliente; }

    public PagoEntity getPago() { return pago; }
    public void setPago(PagoEntity pago) { this.pago = pago; }
}

/**
 * Summary:
 * This entity represents service requests made by clients. Each request stores information 
 * such as the creation date, current state, client details, delivery address, optional estimated 
 * completion date, and associated payment. The entity links to the service being requested, 
 * enabling the system to manage multiple requests per service while tracking client and payment details.
 */
