package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServicioService {
    void save(ServicioEntity servicio);
    ServicioEntity findById(Long idServicio);
    List<ServicioEntity> findPendientesByProveedor(Long idUsuario);
    List<ServicioEntity> findHistorialByProveedor(Long idUsuario);
    List<ServicioEntity> findByCliente(UsuarioEntity cliente);
    List<ServicioEntity> findAll();
    void deleteById(Long id);
    List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre);

    // Solo disponibles (no paginado)
    List<ServicioEntity> findDisponibles();
    List<ServicioEntity> findDisponiblesPorNombre(String nombre);

    // ===== NUEVO: paginados =====
    Page<ServicioEntity> listar(Pageable pageable);                         // todos
    Page<ServicioEntity> listarDisponibles(Pageable pageable);              // solo DISPONIBLES

    // ===== NUEVO: para PROVEEDOR =====
    List<ServicioEntity> findByProveedor(Long idUsuario);
    List<ServicioEntity> findByProveedorAndNombreContainingIgnoreCase(Long idUsuario, String nombre);
}