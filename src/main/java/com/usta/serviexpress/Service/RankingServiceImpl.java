package com.usta.serviexpress.Service;

import com.usta.serviexpress.Repository.CalificacionRepository;
import com.usta.serviexpress.Repository.CalificacionRepository.TopProveedorView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RankingServiceImpl
 *
 * Purpose:
 * - Provides services related to ranking of providers based on ratings (calificaciones).
 * - Retrieves top providers according to a minimum number of reviews and a specified limit.
 *
 * Notes:
 * - Uses CalificacionRepository to fetch aggregated ranking data.
 * - Pagination is applied to limit the number of results returned.
 */
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    // Repository for accessing rating data and aggregated provider rankings
    private final CalificacionRepository calificacionRepository;

    /**
     * Retrieves the top N providers with at least a minimum number of reviews.
     *
     * @param n          Maximum number of providers to return
     * @param minResenas Minimum number of reviews required for a provider to be considered
     * @return List of TopProveedorView objects representing the top providers
     *
     * Notes:
     * - The repository method returns a Page<TopProveedorView>; we extract the content as a List.
     * - Sorting and ranking logic is assumed to be handled in the repository query.
     */
    @Override
    public List<TopProveedorView> topProveedores(int n, long minResenas) {
        return calificacionRepository
                .findTopProveedores(minResenas, PageRequest.of(0, n))
                .getContent(); // Convert Page to List for service layer usage
    }
}

/*
Summary (Technical Note):
RankingServiceImpl provides a simple service to fetch the top-rated providers.
It relies on CalificacionRepository's aggregated queries and enforces a minimum
number of reviews. Pagination ensures that only the top N providers are returned.
The service returns a plain List of TopProveedorView objects suitable for further
processing or exposure via an API.
*/
