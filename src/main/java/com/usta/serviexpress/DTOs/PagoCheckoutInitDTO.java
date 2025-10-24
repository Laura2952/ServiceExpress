package com.usta.serviexpress.DTOs;

import com.usta.serviexpress.Entity.PagoEntity;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoCheckoutInitDTO {
    @NotNull private Long idSolicitud;
    @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal monto;
    @NotNull private PagoEntity.MetodoPago metodo; // TARJETA, PSE, etc.
    @Size(max = 140) private String descripcion;
    @NotBlank private String returnUrl; // front
    @NotBlank private String notifyUrl; // webhook
    @Email @NotBlank private String email;
}