package com.usta.serviexpress.DTOs;

import lombok.Data;

/** Estructura genérica; adapta a la del gateway que uses */
@Data
public class PagoWebhookDTO {
    private String referenciaExterna;   // transactionId del gateway
    private String estado;              // APPROVED / DECLINED / PENDING / REFUNDED
    private Long idPago;                // opcional si envías tu ID propio
    private String rawJson;             // payload crudo (si no quieres usar @RequestBody String)
}
