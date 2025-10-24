package com.usta.serviexpress.payments.core;

import com.usta.serviexpress.payments.config.WompiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WompiSignatureService {

    private final WompiProperties props;

    public String currency() { return props.getCurrency(); }
    public String publicKey() { return props.getPublicKey(); }
    public String redirectUrl() { return props.getRedirectUrl(); }

    /** Precio en COP (con decimales) -> centavos (long) */
    public long toCents(BigDecimal precioCop) {
        return precioCop.movePointRight(2).setScale(0, BigDecimal.ROUND_HALF_UP).longValueExact();
    }

    /** Genera referencia única (usa lo que prefieras: ID solicitud + timestamp) */
    public String buildReference(Long idSolicitud) {
        return "SOL-" + idSolicitud + "-" + System.currentTimeMillis();
    }

    /** Expiración opcional (recomendado). 15 minutos */
    public String expirationIsoUtc() {
        return Instant.now().plus(15, ChronoUnit.MINUTES).toString(); // ISO8601 UTC
    }

    /** Firma de integridad Wompi (sin expiración) */
    public String buildIntegrityNoExp(String reference, long amountInCents) {
        String base = reference + amountInCents + props.getCurrency() + props.getIntegritySecret();
        return sha256Hex(base);
    }

    /** Firma de integridad Wompi (con expiración) */
    public String buildIntegrityWithExp(String reference, long amountInCents, String expirationIso) {
        String base = reference + amountInCents + props.getCurrency() + expirationIso + props.getIntegritySecret();
        return sha256Hex(base);
    }

    private String sha256Hex(String base) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular SHA-256", e);
        }
    }
}