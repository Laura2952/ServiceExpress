package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface SolicitudServicioDAO extends JpaRepository<SolicitudServicioEntity, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.idSolicitud = ?1")
    SolicitudServicioEntity viewDetail(Long idSolicitud);

    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.servicio.idServicio = ?1")
    List<SolicitudServicioEntity> listByServicio(Long idServicio);

    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.estado = ?1")
    List<SolicitudServicioEntity> listByEstado(String estado);

    @Transactional(readOnly = true)
    @Query("SELECT SS FROM SolicitudServicioEntity SS WHERE SS.fechaSolicitud = ?1")
    List<SolicitudServicioEntity> listByFecha(LocalDate fecha);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SolicitudServicioEntity SS SET SS.estado = ?2 WHERE SS.idSolicitud = ?1")
    void updateEstado(Long idSolicitud, String nuevoEstado);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SolicitudServicioEntity SS WHERE SS.idSolicitud = ?1")
    void removeById(Long idSolicitud);
}