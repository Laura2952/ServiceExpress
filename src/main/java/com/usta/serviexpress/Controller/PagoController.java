package com.usta.serviexpress.Controller;

import com.usta.serviexpress.DTOs.PagoCheckoutInitDTO;
import com.usta.serviexpress.Entity.PagoEntity;
import com.usta.serviexpress.Service.PagoService;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> iniciarCheckout(@Valid @RequestBody PagoCheckoutInitDTO dto) {
        String url = pagoService.iniciarCheckout(dto);
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody String payload) {
        pagoService.procesarWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoEntity> get(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.getById(id));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(pagoService.listAll());
    }
}
