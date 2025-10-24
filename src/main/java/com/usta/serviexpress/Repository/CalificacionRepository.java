package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.CalificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<CalificacionEntity, Long> {

    @Query("SELECT c FROM CalificacionEntity c WHERE c.cliente.idUsuario = :idCliente")
    List<CalificacionEntity> listByCliente(@Param("idCliente") Long idCliente);

    List<CalificacionEntity> findByPuntuacionOrderByFechaDesc(Integer puntuacion);

    @Query("""
           SELECT p.idUsuario      AS idProveedor,
                  p.nombreUsuario  AS nombreProveedor,
                  AVG(c.puntuacion) AS promedio,
                  COUNT(c.idCalificacion) AS total
           FROM CalificacionEntity c
           JOIN c.proveedor p
           GROUP BY p.idUsuario, p.nombreUsuario
           HAVING COUNT(c.idCalificacion) >= :minResenas
           """)
    Page<TopProveedorView> findTopProveedores(@Param("minResenas") long minResenas, Pageable pageable);

    interface TopProveedorView {
        Long   getIdProveedor();
        String getNombreProveedor();
        Double getPromedio();
        Long   getTotal();
    }

    Optional<CalificacionEntity> findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
            Long idCliente, Long idProveedor, Long idServicio);

    Optional<CalificacionEntity> findByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
            Long idCliente, Long idProveedor);

    boolean existsByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicio_IdServicio(
            Long idCliente, Long idProveedor, Long idServicio);

    boolean existsByCliente_IdUsuarioAndProveedor_IdUsuarioAndServicioIsNull(
            Long idCliente, Long idProveedor);
}