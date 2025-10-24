package com.usta.serviexpress.Repository;

import com.usta.serviexpress.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    Optional<UsuarioEntity> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    @Query("""
       select u
       from UsuarioEntity u
       left join fetch u.rol
       where lower(u.correo) = lower(:correo)
       """)
    Optional<UsuarioEntity> findByCorreoIgnoreCaseFetchRol(@Param("correo") String correo);

    List<UsuarioEntity> findByRol_Rol(String rol);
}
