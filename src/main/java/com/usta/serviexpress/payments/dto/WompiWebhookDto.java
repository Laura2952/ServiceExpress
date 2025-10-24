package com.usta.serviexpress.payments.dto;

import lombok.Data;

import java.util.Map;

@Data
public class WompiWebhookDto {
    private String event;           // p.ej. "transaction.updated"
    private Map<String,Object> data; // dentro vendrá "transaction": { id, status, reference, amount_in_cents, ... }
    private String timestamp;
    private String signature; // si validas firma del webhook
}