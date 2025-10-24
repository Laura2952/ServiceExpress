package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.ServicioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<ServicioEntity, Long> {

    // PENDIENTES por proveedor (nota: estado es un enum)
    List<ServicioEntity> findByProveedor_IdUsuarioAndEstado(Long idProveedor,
                                                            ServicioEntity.EstadoServicio estado);

    // Historial por proveedor
    List<ServicioEntity> findByProveedor_IdUsuario(Long idProveedor);

    // Servicios solicitados por un cliente (relación directa en ServicioEntity)
    List<ServicioEntity> findByCliente(com.usta.serviexpress.Entity.UsuarioEntity cliente);

    // Buscar por nombre (no paginado)
    List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre);

    // ===== NUEVO: disponibles (no paginado) =====
    List<ServicioEntity> findByEstado(ServicioEntity.EstadoServicio estado);

    // ===== NUEVO: disponibles (paginado) =====
    Page<ServicioEntity> findByEstado(ServicioEntity.EstadoServicio estado, Pageable pageable);

    // ===== NUEVO: disponibles + nombre (no paginado) =====
    List<ServicioEntity> findByEstadoAndNombreContainingIgnoreCase(ServicioEntity.EstadoServicio estado, String nombre);

    // Contar servicios de un proveedor
    long countByProveedor_IdUsuario(Long idProveedor);


    // Buscar por id del proveedor y nombre (ignorar mayúsculas/minúsculas)
    List<ServicioEntity> findByProveedor_IdUsuarioAndNombreContainingIgnoreCase(Long idUsuario, String nombre);

}