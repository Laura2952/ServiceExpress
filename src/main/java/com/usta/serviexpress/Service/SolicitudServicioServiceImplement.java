package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudServicioServiceImplement implements SolicitudServicioService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findAll() {
        return solicitudRepository.findAllDeep(); // ← importante
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudServicioEntity findById(Long id) {
        return solicitudRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(SolicitudServicioEntity solicitud) {
        solicitudRepository.save(solicitud);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        solicitudRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente) {
        return solicitudRepository.findByClienteDeep(cliente); // ← importante
    }

    @Override
    @Transactional
    public void actualizarSolicitudServicio(SolicitudServicioEntity solicitud) {
        if (solicitud.getIdSolicitud() != null && solicitudRepository.existsById(solicitud.getIdSolicitud())) {
            solicitudRepository.save(solicitud);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByProveedorId(Long idProveedor) {
        return solicitudRepository.findByServicio_Proveedor_IdUsuario(idProveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> obtenerSolicitudesPendientes() {
        return solicitudRepository.findByEstado("PENDIENTE");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> obtenerSolicitudesPorUsuario(UsuarioEntity usuario) {
        return solicitudRepository.findByClienteDeep(usuario); // ← importante
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioEntity> findByProveedor(UsuarioEntity proveedor) {
        return solicitudRepository.findByProveedorDeep(proveedor); // ← importante
    }
}