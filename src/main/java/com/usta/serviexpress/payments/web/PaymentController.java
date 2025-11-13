package com.usta.serviexpress.payments.web;

import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Service.SolicitudServicioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * PaymentController
 *
 * Purpose:
 * - Handles user interactions for initiating payments via Wompi.
 * - Prepares invoice data, calculates totals, generates a Wompi signature, and passes all necessary
 *   information to the frontend for checkout.
 *
 * Notes:
 * - Uses session to verify logged-in user.
 * - Calculates total including base service price and delivery fee.
 * - Ensures minimum amount is respected.
 * - Generates SHA-256 integrity signature required by Wompi for security.
 * - Returns a Thymeleaf template ("checkout_wompi") with payment data for frontend integration.
 *
 * Limitations / Considerations:
 * - Assumes user session exists; redirects to login if missing.
 * - Amounts are in Colombian pesos converted to centavos (long).
 * - Expiration for Wompi signature is hard-coded to 20 minutes.
 */
@Controller
@RequiredArgsConstructor
public class PaymentController {

    /** Service to fetch service request (solicitud) data from the database. */
    private final SolicitudServicioService solicitudServicioService;

    /** Wompi public key for client-side integration. */
    @Value("${wompi.public-key}")       
    private String wompiPublicKey;

    /** Secret key for HMAC-SHA256 signature validation. */
    @Value("${wompi.integrity-secret}") 
    private String wompiIntegritySecret;

    /** Currency code for payments, e.g., "COP". */
    @Value("${wompi.currency}")         
    private String currency;

    /** Redirect URL after payment completion. */
    @Value("${wompi.redirect-url}")     
    private String redirectUrl;

    /** Delivery fee in cents (default $10,000 COP). */
    @Value("${wompi.delivery-fee-cents:1000000}") 
    private long deliveryFeeCents;

    /** Minimum allowed payment amount in cents (default $5,000 COP). */
    @Value("${wompi.min-amount-cents:500000}")    
    private long minAmountCents;

    /** ISO8601 UTC formatter for Wompi expiration field. */
    private static final DateTimeFormatter ISO_Z =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneOffset.UTC);

    /**
     * Initiates a Wompi payment session for a given service request.
     *
     * @param solicitudId The ID of the service request (SolicitudServicioEntity) to pay.
     * @param model Spring MVC model to pass attributes to the view.
     * @param session HTTP session to verify logged-in user and store session info.
     * @return Thymeleaf template name "checkout_wompi" or redirects if user not logged in or request not found.
     * @throws Exception If SHA-256 computation fails (should not happen under normal circumstances).
     *
     * Process:
     * 1. Check if user session exists; redirect to login if absent.
     * 2. Retrieve service request from DB; redirect with error if missing.
     * 3. Convert service price to centavos and add delivery fee.
     * 4. Ensure total respects minimum amount.
     * 5. Generate unique reference and expiration timestamp in ISO UTC format.
     * 6. Build integrity signature required by Wompi.
     * 7. Populate model with all necessary values for the frontend payment form.
     */
    @GetMapping("/checkout/wompi/{solicitudId}")
    public String iniciarPago(@PathVariable Long solicitudId, Model model, HttpSession session) throws Exception {
        var usuario = session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(solicitudId);
        if (solicitud == null) return "redirect:/solicitud/historial?error=Solicitud no encontrada";

        // Convert service price to centavos (integer)
        var precio = solicitud.getServicio().getPrecio();
        long baseInCents = precio == null ? 0 :
                precio.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        // Total amount = base + delivery fee, ensuring minimum amount
        long totalInCents = Math.max(baseInCents + deliveryFeeCents, minAmountCents);

        // Generate unique reference and ISO UTC expiration
        String reference = "SOL-" + solicitudId + "-" + System.currentTimeMillis();
        String expirationIso = ISO_Z.format(Instant.now().plus(20, ChronoUnit.MINUTES));

        // Compute SHA-256 signature: <reference><amount><currency><expirationIso><integritySecret>
        String toSign = reference + totalInCents + currency + expirationIso + wompiIntegritySecret;
        String signature = sha256Hex(toSign);

        // Add data for invoice rendering
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("baseInCents", baseInCents);
        model.addAttribute("deliveryFeeCents", deliveryFeeCents);
        model.addAttribute("totalInCents", totalInCents);

        // Add data for Wompi checkout integration
        model.addAttribute("publicKey", wompiPublicKey);
        model.addAttribute("currency", currency);
        model.addAttribute("reference", reference);
        model.addAttribute("signature", signature);
        model.addAttribute("redirectUrl", redirectUrl);
        model.addAttribute("expirationIso", expirationIso);

        return "checkout_wompi"; // Returns template with invoice + Wompi checkout button
    }

    /**
     * Computes SHA-256 hash of the given string and returns it as a hex string.
     *
     * @param s Input string to hash.
     * @return Hexadecimal representation of SHA-256 digest.
     * @throws Exception If SHA-256 algorithm is unavailable (should not happen in standard JVM).
     */
    private static String sha256Hex(String s) throws Exception {
        byte[] dig = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(dig.length * 2);
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

/*
Summary (Technical Note):
PaymentController handles initiating Wompi payment sessions for service requests. It ensures the user
is logged in, calculates totals including delivery fees, enforces minimum amounts, generates unique
references, computes the required SHA-256 signature for Wompi, and passes all necessary information
to the Thymeleaf template "checkout_wompi". The controller is session-aware and integrates directly
with the SolicitudServicioService to fetch request data.
*/
