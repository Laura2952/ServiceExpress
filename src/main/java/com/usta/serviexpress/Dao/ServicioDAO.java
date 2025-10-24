package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServicioDAO extends JpaRepository<ServicioEntity, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE S.idServicio = ?1")
    ServicioEntity viewDetail(Long idServicio);

    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE S.estado = ?1")
    List<ServicioEntity> listByEstado(String estado);

    @Transactional(readOnly = true)
    @Query("SELECT S FROM ServicioEntity S WHERE LOWER(S.nombre) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<ServicioEntity> searchByNombre(String nombre);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ServicioEntity S SET S.nombre = ?2, S.descripcion = ?3, S.precio = ?4, S.estado = ?5 WHERE S.idServicio = ?1")
    void updateServicio(Long idServicio, String nuevoNombre, String nuevaDescripcion, Double nuevoPrecio, String nuevoEstado);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ServicioEntity S WHERE S.idServicio = ?1")
    void removeById(Long idServicio);
}