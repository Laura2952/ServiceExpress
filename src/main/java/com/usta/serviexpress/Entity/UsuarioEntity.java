package com.usta.serviexpress.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo")
)
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class UsuarioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public UsuarioEntity() { }

    public UsuarioEntity(String correo, String clave, String nombreUsuario) {
        this.correo = correo;
        this.clave = clave;
        this.nombreUsuario = nombreUsuario;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // usa la secuencia/default de la BD
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "clave", nullable = false, length = 120)
    private String clave;

    @Column(name = "nombre_usuario", nullable = false, length = 80)
    private String nombreUsuario;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "ciudad", length = 80)
    private String ciudad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)  // <- sin insertable/updatable en false
    private RolEntity rol;

    // (si algún día necesitas setters para proveedor, reemplázalos por campos reales)
    public void setDisponibilidad(boolean disponibilidad) { }
    public void setTarifa(double tarifa) { }
}