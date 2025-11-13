package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Service.RankingService;
import com.usta.serviexpress.Service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * HomeController
 *
 * Purpose:
 * - Manages the main landing page ("/" or "/index") of the ServiExpress application.
 * - Displays a paginated list of available services and a ranking of top-rated providers.
 *
 * Dependencies:
 * - ServicioService: Provides paginated access to available services.
 * - RankingService: Computes and retrieves top service providers based on ratings or review counts.
 *
 * Features:
 * - Pagination support for service listings.
 * - Sorting by service ID in descending order (most recently added first).
 * - Displays provider rankings with a configurable minimum number of reviews.
 *
 * View:
 * - Renders the "index.html" template, expecting:
 *   - servicios → list of available services.
 *   - currentPage, totalPages → pagination metadata.
 *   - topProveedores → top-rated provider list.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ServicioService servicioService;  // Handles service data access and business logic.
    private final RankingService rankingService;    // Provides provider ranking data.

    /**
     * Displays the home page with available services and top provider rankings.
     *
     * Routes: GET "/", GET "/index"
     *
     * @param page  Optional query parameter (default 0) indicating which page of services to display.
     * @param model Spring Model used to pass attributes to the Thymeleaf (or similar) view.
     * @return The "index" view template (templates/index.html).
     *
     * Pagination details:
     * - Each page contains up to 12 services.
     * - Services are sorted in descending order by ID (latest first).
     *
     * Additional content:
     * - Top providers are retrieved from RankingService with parameters:
     *   (3, 1) → top 3 providers with at least 1 review.
     */
    @GetMapping({"/", "/index"})
    public String home(
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        // Ensure page number is non-negative and create pagination request
        Page<ServicioEntity> pagina = servicioService.listarDisponibles(PageRequest.of(
                Math.max(page, 0),
                12, // Maximum services per page
                Sort.by(Sort.Direction.DESC, "idServicio") // Sort newest first
        ));

        // Populate model with paginated service data
        model.addAttribute("servicios", pagina.getContent());
        model.addAttribute("currentPage", pagina.getNumber());
        model.addAttribute("totalPages", pagina.getTotalPages());

        // Add top 3 providers with at least 1 review
        model.addAttribute("topProveedores", rankingService.topProveedores(3, 1));

        // Return view template for home page
        return "index";
    }
}

/*
Summary (Technical Note):
HomeController serves the main landing page of the ServiExpress platform.
It fetches a paginated list of available services (sorted by newest) and displays the
top 3 ranked providers based on user ratings. The controller supports dynamic pagination
via the 'page' query parameter and injects all required data into the model for rendering
in the "index.html" template.
*/
