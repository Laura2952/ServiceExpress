package com.usta.serviexpress.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "SERVICIO")
public class ServicioEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "nombre", length = 200, nullable = false)
    private String nombre;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal precio;

    public enum EstadoServicio {
        PENDIENTE,
        DISPONIBLE,
        OCUPADO,
        ACEPTADA,
        RECHAZADA,
        CANCELADA,
        COMPLETADA
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoServicio estado = EstadoServicio.PENDIENTE;

    @OneToMany(mappedBy = "servicio", fetch = FetchType.LAZY)
    private List<SolicitudServicioEntity> solicitudes;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private UsuarioEntity proveedor;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private UsuarioEntity cliente;
}