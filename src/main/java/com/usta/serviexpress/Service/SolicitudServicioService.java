package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;

import java.util.List;

public interface SolicitudServicioService {
    List<SolicitudServicioEntity> findAll();
    SolicitudServicioEntity findById(Long id);
    void save(SolicitudServicioEntity solicitud);
    void deleteById(Long id);
    List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente);
    void actualizarSolicitudServicio(SolicitudServicioEntity solicitud);
    List<SolicitudServicioEntity> findByProveedorId(Long idProveedor);
    List<SolicitudServicioEntity> obtenerSolicitudesPendientes();
    List<SolicitudServicioEntity> obtenerSolicitudesPorUsuario(UsuarioEntity usuario);

    // 🔹 Solo la firma (sin implementación aquí)
    List<SolicitudServicioEntity> findByProveedor(UsuarioEntity proveedor);
}