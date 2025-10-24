package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<SolicitudServicioEntity, Long> {

    // ====== EXISTENTES (puedes dejarlos) ======
    List<SolicitudServicioEntity> findByServicio_Proveedor_IdUsuario(Long idProveedor);
    long countByServicio_Proveedor_IdUsuario(Long idProveedor);
    List<SolicitudServicioEntity> findByEstado(String estado);
    List<SolicitudServicioEntity> findByCliente(UsuarioEntity cliente);

    @Query("""
           SELECT s
           FROM SolicitudServicioEntity s
           WHERE s.servicio.proveedor.idUsuario = :idProveedor
           """)
    List<SolicitudServicioEntity> listarPorProveedor(@Param("idProveedor") Long idProveedor);

    List<SolicitudServicioEntity> findByServicio_Proveedor(UsuarioEntity proveedor);

    // ====== NUEVOS (con relaciones cargadas) ======
    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findAllDeep();

    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           where sv.proveedor = :proveedor
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findByProveedorDeep(@Param("proveedor") UsuarioEntity proveedor);

    @Query("""
           select s
           from SolicitudServicioEntity s
           left join fetch s.servicio sv
           left join fetch sv.proveedor p
           left join fetch s.cliente c
           where s.cliente = :cliente
           order by s.fechaSolicitud desc, s.idSolicitud desc
           """)
    List<SolicitudServicioEntity> findByClienteDeep(@Param("cliente") UsuarioEntity cliente);
}