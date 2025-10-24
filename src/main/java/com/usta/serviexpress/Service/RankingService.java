package com.usta.serviexpress.Service;

import com.usta.serviexpress.Repository.CalificacionRepository.TopProveedorView;
import java.util.List;

public interface RankingService {
    List<TopProveedorView> topProveedores(int n, long minResenas);
}