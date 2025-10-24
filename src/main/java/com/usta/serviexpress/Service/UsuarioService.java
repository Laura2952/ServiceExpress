package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.UsuarioEntity;

import java.util.List;

public interface UsuarioService {

    List<UsuarioEntity> findAll();

    UsuarioEntity findById(Long id);

    /** Búsqueda por correo (ignore case). Devuelve null si no existe. */
    UsuarioEntity findByCorreo(String correo);

    /** ¿Existe un usuario con ese correo? (ignore case) */
    boolean existsByCorreo(String correo);

    /**
     * Registra un usuario nuevo hasheando la contraseña en BCrypt.
     * Lanza IllegalArgumentException si el correo ya existe.
     */
    UsuarioEntity registrar(String correo, String nombreUsuario, String passwordPlano, Long idRol);

    /**
     * Guarda/actualiza un usuario.
     * Si la propiedad 'clave' viene en texto plano, se hashea automáticamente.
     */
    UsuarioEntity save(UsuarioEntity usuario);

    void deleteById(Long id);

    /** Cambia la contraseña (recibe en texto plano y la hashea). */
    void changePassword(Long id, String nuevaContrasena);

    // Nuevo: listar solo proveedores
    List<UsuarioEntity> findAllProveedores();
}