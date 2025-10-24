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

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ServicioService servicioService;
    private final RankingService rankingService;

    @GetMapping({"/", "/index"})
    public String home(
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        Page<ServicioEntity> pagina = servicioService.listarDisponibles(PageRequest.of(
                Math.max(page, 0),
                12,
                Sort.by(Sort.Direction.DESC, "idServicio")
        ));

        model.addAttribute("servicios", pagina.getContent());
        model.addAttribute("currentPage", pagina.getNumber());
        model.addAttribute("totalPages", pagina.getTotalPages());

        model.addAttribute("topProveedores", rankingService.topProveedores(3, 1)); // mínimo 1 reseña

        return "index"; // templates/index.html
    }
}