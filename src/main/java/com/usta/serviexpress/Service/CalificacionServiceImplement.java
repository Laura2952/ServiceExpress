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

/**
 * CalificacionServiceImplement
 *
 * Purpose:
 * - Implements business logic for managing ratings (calificaciones) in the system.
 * - Allows creating new ratings for services or providers.
 * - Supports listing ratings by criteria.
 */
@Service
@RequiredArgsConstructor
public class CalificacionServiceImplement implements CalificacionService {

    private final CalificacionRepository calificacionRepo;
    private final UsuarioRepository usuarioRepo;
    private final ServicioRepository servicioRepo;

    /**
     * Creates a new rating (calificación) for a provider or service.
     * If a rating already exists for the same client-provider-service combination,
     * it updates the existing record instead of creating a new one.
     *
     * @param idCliente ID of the client submitting the rating
     * @param dto       DTO containing rating details
     *                  - dto.getPuntuacion(): rating score (1-5)
     *                  - dto.getComentario(): optional comment
     *                  - dto.getProveedorId(): optional provider ID
     *                  - dto.getServicioId(): optional service ID
     *
     * @throws IllegalArgumentException if input data is invalid
     */
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

        // Retrieve client reference (lazy-loaded)
        UsuarioEntity cliente = usuarioRepo.getReferenceById(idCliente);

        ServicioEntity servicio = null;
        UsuarioEntity proveedor;

        // Determine if rating is for a specific service or only provider
        if (dto.getServicioId() != null) {
            servicio = servicioRepo.getReferenceById(dto.getServicioId());
            proveedor = servicio.getProveedor();
        } else {
            proveedor = usuarioRepo.getReferenceById(dto.getProveedorId());
        }

        // Check if a rating already exists for this client-provider(-service) combination
        Optional<CalificacionEntity> existente = (servicio != null)
                ? calificacionRepo.findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
                        cliente.getIdUsuario(), proveedor.getIdUsuario(), servicio.getIdServicio())
                : calificacionRepo.findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
                        cliente.getIdUsuario(), proveedor.getIdUsuario());

        // If exists, update it; otherwise create a new rating
        CalificacionEntity c = existente.orElseGet(CalificacionEntity::new);
        c.setCliente(cliente);
        c.setProveedor(proveedor);
        c.setServicio(servicio);
        c.setPuntuacion(dto.getPuntuacion());
        c.setComentario(dto.getComentario());
        c.setFecha(LocalDateTime.now());

        // Persist the rating
        calificacionRepo.save(c);
    }

    /**
     * Returns a list of all ratings in the system.
     *
     * @return List of CalificacionEntity
     */
    @Override
    public List<CalificacionEntity> listarTodas() {
        return calificacionRepo.findAll();
    }

    /**
     * Returns a list of ratings filtered by a specific score (puntuación),
     * ordered by most recent first.
     *
     * @param puntuacion the score to filter by (1-5)
     * @return List of CalificacionEntity matching the score
     */
    @Override
    public List<CalificacionEntity> listarPorPuntuacion(int puntuacion) {
        return calificacionRepo.findByPuntuacionOrderByFechaDesc(puntuacion);
    }
}
