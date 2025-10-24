package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.RolRepository;
import com.usta.serviexpress.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImplement implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioEntity> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public UsuarioEntity findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public UsuarioEntity findByCorreo(String correo) {
        // Trae también el rol (left join fetch)
        return usuarioRepository.findByCorreoIgnoreCaseFetchRol(correo).orElse(null);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    @Transactional
    public UsuarioEntity registrar(String correo, String nombreUsuario, String passwordPlano, Long idRol) {
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("Correo es obligatorio");
        if (passwordPlano == null || passwordPlano.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        if (existsByCorreo(correo))
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");

        RolEntity rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        UsuarioEntity u = new UsuarioEntity();
        u.setCorreo(correo.trim());
        u.setNombreUsuario(nombreUsuario != null ? nombreUsuario.trim() : null);
        u.setClave(passwordEncoder.encode(passwordPlano));
        u.setRol(rol);

        return usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public UsuarioEntity save(UsuarioEntity usuario) {
        // Si la clave viene en texto plano, se hashea
        String p = usuario.getClave();
        if (p != null && !p.isBlank()
                && !p.startsWith("$2a$") && !p.startsWith("$2b$") && !p.startsWith("$2y$")) {
            usuario.setClave(passwordEncoder.encode(p));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.isBlank())
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");

        UsuarioEntity u = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setClave(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(u);
    }

    @Override
    public List<UsuarioEntity> findAllProveedores() {
        // Si en DB el rol está como "PROVEEDOR"
        return usuarioRepository.findByRol_Rol("PROVEEDOR");
    }
}