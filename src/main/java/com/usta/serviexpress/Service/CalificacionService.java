package com.usta.serviexpress.Service;

import com.usta.serviexpress.DTOs.CalificacionCreateDTO;
import com.usta.serviexpress.Entity.CalificacionEntity;

import java.util.List;

public interface CalificacionService {
    void crear(Long idCliente, CalificacionCreateDTO dto);
    List<CalificacionEntity> listarTodas();
    List<CalificacionEntity> listarPorPuntuacion(int puntuacion);
}