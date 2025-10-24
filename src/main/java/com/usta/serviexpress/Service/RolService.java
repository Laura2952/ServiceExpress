package com.usta.serviexpress.Service;

import com.usta.serviexpress.Entity.RolEntity;

import java.util.List;

public interface RolService {
    List<RolEntity> findAll();
    RolEntity findById(Long id);
    void save(RolEntity rol);
    void deleteById(Long id);
}
