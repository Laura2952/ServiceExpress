package com.usta.serviexpress.payments.core;

import com.usta.serviexpress.payments.config.WompiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * WompiSignatureService
 *
 * Purpose:
 * - Centralizes generation of integrity signatures and auxiliary data for Wompi payments.
 * - Handles reference building, currency conversion, expiration timestamps, and SHA-256 signature creation.
 *
 * Notes:
 * - All signatures are based on Wompi's recommended algorithm: concatenation of reference, amount, currency,
 *   optional expiration, and integrity secret, then SHA-256 hashing.
 * - This service does NOT make network calls; it's purely cryptographic and formatting logic.
 */
@Service
@RequiredArgsConstructor
public class WompiSignatureService {

    private final WompiProperties props;

    /** Retrieves default currency configured in Wompi properties */
    public String currency() {
        return props.getCurrency();
    }

    /** Retrieves public key from configuration */
    public String publicKey() {
        return props.getPublicKey();
    }

    /** Retrieves redirect URL from configuration */
    public String redirectUrl() {
        return props.getRedirectUrl();
    }

    /**
     * Convierte un precio en COP con decimales a centavos (long)
     * @param precioCop precio en COP como BigDecimal
     * @return precio en centavos
     */
    public long toCents(BigDecimal precioCop) {
        return precioCop
                .movePointRight(2) // COP -> centavos
                .setScale(0, BigDecimal.ROUND_HALF_UP) // redondeo
                .longValueExact();
    }

    /**
     * Genera referencia única para la transacción
     * @param idSolicitud ID de la solicitud
     * @return referencia tipo "SOL-{idSolicitud}-{timestamp}"
     */
    public String buildReference(Long idSolicitud) {
        return "SOL-" + idSolicitud + "-" + System.currentTimeMillis();
    }

    /**
     * Expiración opcional para pagos (recomendado 15 minutos)
     * @return ISO8601 UTC timestamp
     */
    public String expirationIsoUtc() {
        return Instant.now().plus(15, ChronoUnit.MINUTES).toString();
    }

    /**
     * Genera firma de integridad sin expiración
     * @param reference referencia de la transacción
     * @param amountInCents cantidad en centavos
     * @return SHA-256 hexadecimal
     */
    public String buildIntegrityNoExp(String reference, long amountInCents) {
        String base = reference + amountInCents + props.getCurrency() + props.getIntegritySecret();
        return sha256Hex(base);
    }

    /**
     * Genera firma de integridad con expiración
     * @param reference referencia de la transacción
     * @param amountInCents cantidad en centavos
     * @param expirationIso timestamp de expiración ISO8601 UTC
     * @return SHA-256 hexadecimal
     */
    public String buildIntegrityWithExp(String reference, long amountInCents, String expirationIso) {
        String base = reference + amountInCents + props.getCurrency() + expirationIso + props.getIntegritySecret();
        return sha256Hex(base);
    }

    /** Calcula hash SHA-256 y devuelve hexadecimal */
    private String sha256Hex(String base) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular SHA-256", e);
        }
    }
}
