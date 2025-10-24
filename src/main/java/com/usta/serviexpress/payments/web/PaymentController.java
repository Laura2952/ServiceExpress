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

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final SolicitudServicioService solicitudServicioService;

    @Value("${wompi.public-key}")       private String wompiPublicKey;
    @Value("${wompi.integrity-secret}") private String wompiIntegritySecret;
    @Value("${wompi.currency}")         private String currency;
    @Value("${wompi.redirect-url}")     private String redirectUrl;
    @Value("${wompi.delivery-fee-cents:1000000}") private long deliveryFeeCents; // $10.000
    @Value("${wompi.min-amount-cents:500000}")    private long minAmountCents;   // $5.000

    private static final DateTimeFormatter ISO_Z =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneOffset.UTC);

    /** FACTURA + redirección a Wompi */
    @GetMapping("/checkout/wompi/{solicitudId}")
    public String iniciarPago(@PathVariable Long solicitudId, Model model, HttpSession session) throws Exception {
        var usuario = session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(solicitudId);
        if (solicitud == null) return "redirect:/solicitud/historial?error=Solicitud no encontrada";

        // base en centavos
        var precio = solicitud.getServicio().getPrecio();
        long baseInCents = precio == null ? 0 :
                precio.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        // total = base + domicilio (y respeta mínimo)
        long totalInCents = Math.max(baseInCents + deliveryFeeCents, minAmountCents);

        // referencia + expiración ISO UTC
        String reference = "SOL-" + solicitudId + "-" + System.currentTimeMillis();
        String expirationIso = ISO_Z.format(Instant.now().plus(20, ChronoUnit.MINUTES)); // obligatorio en la firma

        // Firma Wompi (SHA-256): <ref><amount><currency><expirationIso><integritySecret>
        String toSign = reference + totalInCents + currency + expirationIso + wompiIntegritySecret;
        String signature = sha256Hex(toSign);

        model.addAttribute("solicitud", solicitud);

        // para factura
        model.addAttribute("baseInCents", baseInCents);
        model.addAttribute("deliveryFeeCents", deliveryFeeCents);
        model.addAttribute("totalInCents", totalInCents);

        // para checkout web
        model.addAttribute("publicKey", wompiPublicKey);
        model.addAttribute("currency", currency);
        model.addAttribute("reference", reference);
        model.addAttribute("signature", signature);
        model.addAttribute("redirectUrl", redirectUrl);
        model.addAttribute("expirationIso", expirationIso);

        return "checkout_wompi"; // factura + botón "Pagar con Wompi"
    }

    private static String sha256Hex(String s) throws Exception {
        byte[] dig = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(dig.length * 2);
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}