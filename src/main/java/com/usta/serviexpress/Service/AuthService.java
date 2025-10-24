package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.RegistroClienteDTO;
import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Repository.RolRepository;
import com.usta.serviexpress.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioEntity registrarCliente(RegistroClienteDTO dto) {
        // 1) contraseñas iguales
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        // 2) correo único
        if (usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        // 3) rol CLIENTE (lo crea si no existe)
        RolEntity rolCliente = rolRepository.findByRolIgnoreCase("CLIENTE")
                .orElseGet(() -> {
                    RolEntity r = new RolEntity();
                    r.setRol("CLIENTE");
                    return rolRepository.save(r);
                });

        // 4) construir usuario
        UsuarioEntity u = new UsuarioEntity();
        u.setNombreUsuario(dto.getNombre());
        u.setCorreo(dto.getCorreo().toLowerCase());
        u.setTelefono(dto.getTelefono());
        u.setCiudad(dto.getCiudad());
        u.setClave(passwordEncoder.encode(dto.getPassword()));
        u.setRol(rolCliente);

        return usuarioRepository.save(u);
    }
}