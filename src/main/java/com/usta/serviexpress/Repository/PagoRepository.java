package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<PagoEntity, Long> {

    // Buscar por token de pago (para iniciar checkout)
    Optional<PagoEntity> findByPaymentToken(String paymentToken);

    // Buscar por referencia externa (Wompi/otra pasarela)
    Optional<PagoEntity> findByReferenciaExterna(String referenciaExterna);

    // Buscar todos los pagos asociados a un correo de cliente
    List<PagoEntity> findByEmailCliente(String emailCliente);

    // Buscar todos los pagos por estado (ej: PENDIENTE, APROBADO)
    List<PagoEntity> findByEstado(PagoEntity.EstadoPago estado);
}