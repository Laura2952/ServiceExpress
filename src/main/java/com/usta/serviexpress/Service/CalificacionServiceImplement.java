package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.CalificacionCreateDTO;
import com.usta.serviexpress.Entity.CalificacionEntity;
import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.CalificacionRepository;
import com.usta.serviexpress.Repository.ServicioRepository;
import com.usta.serviexpress.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalificacionServiceImplement implements CalificacionService {

    private final CalificacionRepository calificacionRepo;
    private final UsuarioRepository usuarioRepo;
    private final ServicioRepository servicioRepo;

    @Override
    @Transactional
    public void crear(Long idCliente, CalificacionCreateDTO dto) {
        if (idCliente == null) throw new IllegalArgumentException("No se pudo identificar al cliente.");
        if (dto == null) throw new IllegalArgumentException("Datos inválidos.");
        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
        }
        if (dto.getProveedorId() == null && dto.getServicioId() == null) {
            throw new IllegalArgumentException("Debes indicar un proveedor o un servicio.");
        }

        UsuarioEntity cliente = usuarioRepo.getReferenceById(idCliente);

        ServicioEntity servicio = null;
        UsuarioEntity proveedor;

        if (dto.getServicioId() != null) {
            servicio = servicioRepo.getReferenceById(dto.getServicioId());
            proveedor = servicio.getProveedor();
        } else {
            proveedor = usuarioRepo.getReferenceById(dto.getProveedorId());
        }

        Optional<CalificacionEntity> existente = (servicio != null)
                ? calificacionRepo.findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
                cliente.getIdUsuario(), proveedor.getIdUsuario(), servicio.getIdServicio())
                : calificacionRepo.findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
                cliente.getIdUsuario(), proveedor.getIdUsuario());

        CalificacionEntity c = existente.orElseGet(CalificacionEntity::new);
        c.setCliente(cliente);
        c.setProveedor(proveedor);
        c.setServicio(servicio);
        c.setPuntuacion(dto.getPuntuacion());
        c.setComentario(dto.getComentario());
        c.setFecha(LocalDateTime.now());

        calificacionRepo.save(c);
    }

    @Override
    public List<CalificacionEntity> listarTodas() {
        return calificacionRepo.findAll();
    }

    @Override
    public List<CalificacionEntity> listarPorPuntuacion(int puntuacion) {
        return calificacionRepo.findByPuntuacionOrderByFechaDesc(puntuacion);
    }
}