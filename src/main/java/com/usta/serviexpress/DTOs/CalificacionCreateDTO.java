package com.usta.serviexpress.DTOs;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * CalificacionCreateDTO
 *
 * Purpose:
 * - Data Transfer Object (DTO) used to create a new rating (calificación) for a service or provider.
 * - Encapsulates the rating score, optional comment, and target entity (service or provider).
 * - Includes validation annotations to ensure correct input before persistence.
 *
 * Important considerations:
 * - Either 'servicioId' or 'proveedorId' must be provided, but not necessarily both.
 * - Rating score must be between 1 and 5 inclusive.
 * - Comment is optional but limited to 500 characters.
 */
@Getter
@Setter
public class CalificacionCreateDTO {

    /**
     * Identifier of the service being rated.
     * - Optional if 'proveedorId' is provided.
     * - One of the two IDs (servicioId or proveedorId) must be present.
     */
    private Long servicioId;

    /**
     * Identifier of the provider being rated.
     * - Optional if 'servicioId' is provided.
     * - One of the two IDs (servicioId or proveedorId) must be present.
     */
    private Long proveedorId;

    /**
     * Rating score.
     * - Mandatory field.
     * - Valid values: 1 to 5 inclusive.
     *
     * Validation:
     * - @NotNull ensures a value is provided.
     * - @Min and @Max enforce the valid range.
     */
    @NotNull(message = "Selecciona una puntuación de 1 a 5.")
    @Min(value = 1, message = "La puntuación mínima es 1.")
    @Max(value = 5, message = "La puntuación máxima es 5.")
    private Integer puntuacion;

    /**
     * Optional comment about the service or provider.
     * - Maximum length: 500 characters.
     *
     * Validation:
     * - @Size restricts the length to prevent excessively long input.
     */
    @Size(max = 500, message = "El comentario no puede superar 500 caracteres.")
    private String comentario;

    /**
     * Cross-field validation to ensure that at least one target is specified.
     * - Returns true if either 'servicioId' or 'proveedorId' is provided.
     * - Used with @AssertTrue to trigger validation errors when both IDs are null.
     *
     * Returns:
     * - boolean indicating whether the DTO has a valid target.
     */
    @AssertTrue(message = "Debes elegir un servicio o un proveedor.")
    public boolean isDestinoValido() {
        return servicioId != null || proveedorId != null;
    }
}

/*
Summary (Technical Note):
CalificacionCreateDTO is a data transfer object used to submit a new rating for a service or provider.
It validates that either a service or provider ID is specified, the score is between 1 and 5, and
any comment is under 500 characters. Cross-field validation ensures that a rating cannot be created
without specifying a valid target entity.
*/
