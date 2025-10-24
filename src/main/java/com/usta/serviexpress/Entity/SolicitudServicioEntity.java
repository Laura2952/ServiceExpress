package com.usta.serviexpress.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "SOLICITUD_SERVICIO")
public class SolicitudServicioEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "estado", length = 40)
    private String estado; // PENDIENTE, PAGO_EN_PROCESO, PAGO_ACEPTADO, EN PROCESO, FINALIZADO, etc.

    // ===== NUEVOS CAMPOS =====
    @Size(max = 500)
    @Column(name = "detalles", length = 500)
    private String detalles; // especificaciones del cliente

    @Size(max = 180)
    @Column(name = "direccion_entrega", length = 180)
    private String direccionEntrega; // confirmación de dirección

    @Column(name = "fecha_estimada")
    private LocalDate fechaEstimada; // opcional

    // ===== RELACIONES =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private ServicioEntity servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private UsuarioEntity cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago")
    private PagoEntity pago;

    // ===== GETTERS/SETTERS =====
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