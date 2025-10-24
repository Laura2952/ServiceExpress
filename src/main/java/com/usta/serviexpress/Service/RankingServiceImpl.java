package com.usta.serviexpress.Service;

import com.usta.serviexpress.Repository.CalificacionRepository;
import com.usta.serviexpress.Repository.CalificacionRepository.TopProveedorView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final CalificacionRepository calificacionRepository;

    @Override
    public List<TopProveedorView> topProveedores(int n, long minResenas) {
        return calificacionRepository
                .findTopProveedores(minResenas, PageRequest.of(0, n))
                .getContent(); // <-- convertir Page a List
    }
}