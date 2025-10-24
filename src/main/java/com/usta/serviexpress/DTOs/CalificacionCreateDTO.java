package com.usta.serviexpress.DTOs;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalificacionCreateDTO {

    /** Uno de los dos debe venir: servicioId o proveedorId */
    private Long servicioId;
    private Long proveedorId;

    @NotNull(message = "Selecciona una puntuación de 1 a 5.")
    @Min(value = 1, message = "La puntuación mínima es 1.")
    @Max(value = 5, message = "La puntuación máxima es 5.")
    private Integer puntuacion;

    @Size(max = 500, message = "El comentario no puede superar 500 caracteres.")
    private String comentario;

    /** Validación cruzada */
    @AssertTrue(message = "Debes elegir un servicio o un proveedor.")
    public boolean isDestinoValido() {
        return servicioId != null || proveedorId != null;
    }
}