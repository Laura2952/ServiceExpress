package com.usta.serviexpress.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * RolEntity
 *
 * Purpose:
 * - Represents a user role in the system (e.g., ADMIN, CLIENTE, PROVEEDOR).
 * - Used for authorization and access control.
 *
 * Persistence mapping:
 * - Mapped to the "roles" table.
 * - The "rol" column is unique and indexed for fast lookup.
 *
 * Validation:
 * - rol must be non-blank and have up to 80 characters.
 *
 * Serialization:
 * - @JsonIgnoreProperties prevents issues with lazy loading when serializing to JSON.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "roles",
        indexes = {
                @Index(name = "ix_roles_rol", columnList = "rol", unique = true)
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class RolEntity {

    /** Primary key for the role */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    @EqualsAndHashCode.Include
    private Long id;

    /** Name of the role (unique) */
    @NotBlank
    @Column(name = "rol", nullable = false, length = 80, unique = true)
    private String rol;
}
