package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PagoDAO extends JpaRepository<PagoEntity, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.idPago = ?1")
    PagoEntity viewDetail(Long idPago);

    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.referenciaExterna = ?1")
    PagoEntity findByReferenciaExterna(String referenciaExterna);

    @Transactional(readOnly = true)
    @Query("SELECT P FROM PagoEntity P WHERE P.solicitud.idSolicitud = ?1")
    PagoEntity findBySolicitudId(Long idSolicitud);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.estado = ?2 WHERE P.idPago = ?1")
    void changeEstado(Long idPago, PagoEntity.EstadoPago nuevoEstado);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.estado = ?2, P.gatewayPayload = ?3 WHERE P.referenciaExterna = ?1")
    void changeEstadoByReferencia(String referenciaExterna, PagoEntity.EstadoPago nuevoEstado, String rawPayload);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.referenciaExterna = ?2 WHERE P.idPago = ?1")
    void setReferenciaExterna(Long idPago, String referenciaExterna);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PagoEntity P SET P.gatewayPayload = ?2 WHERE P.idPago = ?1")
    void setGatewayPayload(Long idPago, String rawPayload);
}
