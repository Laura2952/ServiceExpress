package com.usta.serviexpress.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "roles",
        indexes = {
                @Index(name = "ix_roles_rol", columnList = "rol", unique = true)
        })
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(name = "rol", nullable = false, length = 80, unique = true)
    private String rol;
}