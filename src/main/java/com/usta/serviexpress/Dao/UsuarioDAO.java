package com.usta.serviexpress.Dao;

import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.UsuarioService;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface UsuarioDAO extends CrudRepository<UsuarioEntity, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE UsuarioEntity SET clave = ?2 WHERE idUsuario = ?1")
    void changePassword(Long idUsuario, String nuevaContrasena);

    @Transactional
    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo = ?1")
    UsuarioEntity findByEmail(String correo);
}


