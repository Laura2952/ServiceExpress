package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;
import com.usta.serviexpress.Repository.PagoRepository;
import com.usta.serviexpress.Repository.SolicitudRepository;
import com.usta.serviexpress.Repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación agnóstica de pasarela.
 * Inyecta aquí tu PaymentProvider real (Wompi/PayU) si lo tienes.
 */
@Service
@RequiredArgsConstructor
public class PagoServiceImplement implements PagoService {

    private final PagoRepository pagoRepo;
    private final SolicitudRepository solicitudRepo;

    // Si ya tienes un provider real, descomenta e inyecta
    // private final PaymentProvider provider;

    @Override
    @Transactional
    public String iniciarCheckout(PagoCheckoutInitDTO dto) {
        var solicitud = solicitudRepo.findById(dto.getIdSolicitud())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        PagoEntity pago = new PagoEntity();
        pago.setSolicitud(solicitud);
        pago.setMonto(dto.getMonto());
        pago.setMetodo(dto.getMetodo());
        pago.setEstado(PagoEntity.EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMoneda("COP");
        pago.setDescripcion(dto.getDescripcion());
        pago = pagoRepo.save(pago);

        // Llama a tu provider para crear checkout y obtener URL
        // CreateCheckoutRequest req = ...
        // var res = provider.createCheckout(req);
        // pago.setReferenciaExterna(res.getTransactionOrSessionId()); // si aplica
        // pagoRepo.save(pago);
        // return res.getCheckoutUrl();

        // Por ahora devolvemos un placeholder para que puedas seguir trabajando:
        return "/pagos/mock-checkout/" + pago.getIdPago();
    }

    @Override
    @Transactional
    public void procesarWebhook(String signature, String payload) {
        // Simulación simple: busca una referencia dentro del payload
        String ref = extraerRef(payload); // implementa tu parser
        String estado = extraerEstado(payload);

        PagoEntity.EstadoPago nuevoEstado = switch (estado.toUpperCase()) {
            case "APPROVED", "APROBADO" -> PagoEntity.EstadoPago.APROBADO;
            case "DECLINED", "FALLIDO" -> PagoEntity.EstadoPago.FALLIDO;
            case "REFUNDED", "REEMBOLSADO" -> PagoEntity.EstadoPago.REEMBOLSADO;
            default -> PagoEntity.EstadoPago.PENDIENTE;
        };
        actualizarEstadoPorReferencia(ref, nuevoEstado, payload);
    }

    @Override
    @Transactional
    public PagoEntity actualizarEstadoPorReferencia(String ref, PagoEntity.EstadoPago estado, String payload) {
        var pago = pagoRepo.findByReferenciaExterna(ref)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado por referencia: " + ref));
        pago.setEstado(estado);
        pago.setGatewayPayload(payload);
        return pagoRepo.save(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoEntity getById(Long id) {
        return pagoRepo.findById(id).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoEntity> listAll() {
        return pagoRepo.findAll();
    }

    // --- Helpers mínimos (implementa tu parseo real del webhook) ---
    private String extraerRef(String payload) {
        // parsea JSON y retorna transactionId / reference
        return "tx_demo_ref";
    }
    private String extraerEstado(String payload) {
        return "APPROVED";
    }
}