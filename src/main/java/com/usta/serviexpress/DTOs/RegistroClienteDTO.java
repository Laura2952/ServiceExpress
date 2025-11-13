package com.usta.serviexpress.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RegistroClienteDTO
 *
 * Purpose:
 * - Data Transfer Object (DTO) for registering a new client.
 * - Encapsulates client information including name, email, city, phone, and password.
 * - Includes validation annotations to enforce required fields and basic constraints.
 *
 * Important considerations:
 * - 'password' requires a minimum length of 6 characters.
 * - 'confirmPassword' must match 'password'; cross-field validation is assumed to be handled elsewhere.
 * - All fields are mandatory.
 */
@Data
public class RegistroClienteDTO {

    /**
     * Client's full name.
     * - Mandatory field.
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /**
     * Client's email address.
     * - Mandatory field.
     * - Must be a valid email format.
     */
    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    /**
     * Client's city of residence.
     * - Mandatory field.
     */
    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    /**
     * Client's phone number.
     * - Mandatory field.
     */
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    /**
     * Client's password.
     * - Mandatory field.
     * - Minimum length: 6 characters.
     * - Should be stored securely (e.g., hashed) when persisted.
     */
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    /**
     * Password confirmation field.
     * - Mandatory.
     * - Should match the 'password' field; cross-field validation logic is assumed to be implemented elsewhere.
     */
    @NotBlank(message = "Confirma tu contraseña")
    private String confirmPassword;
}

/*
Summary (Technical Note):
RegistroClienteDTO is a data transfer object used for client registration. It validates that all 
fields—name, email, city, phone, password, and password confirmation—are provided, that the email
is valid, and that the password meets a minimum length requirement. Cross-field validation (password
matching confirmPassword) is expected to be handled in the service layer or a dedicated validator.
*/
