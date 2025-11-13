package com.usta.serviexpress.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * TokenUtils
 *
 * Propósito / Purpose:
 * - Genera tokens únicos de longitud fija para ser utilizados en autenticación,
 *   validaciones o identificadores temporales.
 * - Generates unique fixed-length tokens for authentication,
 *   validations, or temporary identifiers.
 *
 * Notas importantes / Important Notes:
 * - Los tokens generados no son secretos criptográficos; si se requiere alta seguridad,
 *   usar métodos criptográficos adicionales.
 * - Generated tokens are not cryptographically secure; for high-security requirements,
 *   additional cryptographic methods should be used.
 */
@Component
public class TokenUtils {

    /**
     * Genera un nuevo token de 32 caracteres eliminando los guiones de UUID.
     * Generates a new 32-character token by removing dashes from a UUID.
     *
     * Retorno / Return:
     * @return String - Token único de 32 caracteres
     *                  Unique 32-character token
     *
     * Consideraciones / Notes:
     * - Basado en UUID.randomUUID(), la probabilidad de colisión es extremadamente baja.
     * - Based on UUID.randomUUID(), collision probability is extremely low.
     */
    public String newToken() {
        return UUID.randomUUID().toString().replace("-", ""); // genera un token de 32 chars / generates a 32-char token
    }
}

/*
Resumen técnico / Technical Summary:
TokenUtils provides a simple utility to generate 32-character unique tokens
using UUIDs. Tokens are suitable for temporary identifiers or basic validation purposes,
but are not intended for high-security cryptographic uses.
*/
