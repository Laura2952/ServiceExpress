package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioServiceImplement implements ServicioService {

    private final ServicioRepository servicioRepository;

    @Override
    public void save(ServicioEntity servicio) {
        servicioRepository.save(servicio);
    }

    @Override
    public ServicioEntity findById(Long idServicio) {
        return servicioRepository.findById(idServicio).orElse(null);
    }

    @Override
    public List<ServicioEntity> findPendientesByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuarioAndEstado(
                idUsuario,
                ServicioEntity.EstadoServicio.PENDIENTE
        );
    }

    @Override
    public List<ServicioEntity> findHistorialByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuario(idUsuario);
    }

    @Override
    public List<ServicioEntity> findByCliente(UsuarioEntity cliente) {
        return servicioRepository.findByCliente(cliente);
    }

    @Override
    public List<ServicioEntity> findAll() {
        return servicioRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        servicioRepository.deleteById(id);
    }

    @Override
    public List<ServicioEntity> findByNombreContainingIgnoreCase(String nombre) {
        return servicioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== Solo DISPONIBLES (no paginados) =====
    @Override
    public List<ServicioEntity> findDisponibles() {
        return servicioRepository.findByEstado(ServicioEntity.EstadoServicio.DISPONIBLE);
    }

    @Override
    public List<ServicioEntity> findDisponiblesPorNombre(String nombre) {
        return servicioRepository.findByEstadoAndNombreContainingIgnoreCase(
                ServicioEntity.EstadoServicio.DISPONIBLE, nombre
        );
    }

    // ===== Paginados =====
    @Override
    public Page<ServicioEntity> listar(Pageable pageable) {
        return servicioRepository.findAll(pageable);
    }

    @Override
    public Page<ServicioEntity> listarDisponibles(Pageable pageable) {
        return servicioRepository.findByEstado(ServicioEntity.EstadoServicio.DISPONIBLE, pageable);
    }

    // ===== Nuevos métodos para proveedor =====
    @Override
    public List<ServicioEntity> findByProveedor(Long idUsuario) {
        return servicioRepository.findByProveedor_IdUsuario(idUsuario);
    }

    @Override
    public List<ServicioEntity> findByProveedorAndNombreContainingIgnoreCase(Long idUsuario, String nombre) {
        return servicioRepository.findByProveedor_IdUsuarioAndNombreContainingIgnoreCase(idUsuario, nombre);
    }
}