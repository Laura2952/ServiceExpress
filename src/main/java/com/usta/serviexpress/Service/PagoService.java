package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;

import java.util.List;

public interface PagoService {
    String iniciarCheckout(PagoCheckoutInitDTO dto);      // devuelve url de checkout
    void procesarWebhook(String signature, String payload);
    PagoEntity actualizarEstadoPorReferencia(String ref, PagoEntity.EstadoPago estado, String payload);
    PagoEntity getById(Long id);
    List<PagoEntity> listAll();
}
