package com.usta.serviexpress.Controller;

import com.usta.serviexpress.DTOs.CalificacionCreateDTO;
import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Service.CalificacionService;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.SolicitudServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import com.usta.serviexpress.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final SolicitudServicioService solicitudServicioService;
    private final ServicioService servicioService;
    private final UsuarioService usuarioService;

    /** LISTAR (con filtro opcional por rating) */
    @GetMapping
    public String listar(@RequestParam(name = "rating", required = false) Integer rating,
                         Model model) {
        model.addAttribute("title", "Calificaciones");
        var lista = (rating == null)
                ? calificacionService.listarTodas()
                : calificacionService.listarPorPuntuacion(rating);
        model.addAttribute("calificaciones", lista);
        model.addAttribute("rating", rating);
        return "calificaciones/lista";
    }

    /** FORM libre: /calificaciones/nueva?servicio=...&proveedor=... */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long servicio,
                        @RequestParam(required = false) Long proveedor,
                        Model model) {

        CalificacionCreateDTO dto = new CalificacionCreateDTO();
        dto.setServicioId(servicio);
        dto.setProveedorId(proveedor);

        model.addAttribute("title", "Nueva calificación");
        model.addAttribute("calificacion", dto);

        // Si no vino contexto, cargamos combos
        if (servicio == null && proveedor == null) {
            model.addAttribute("servicios", servicioService.findAll());   // o un top N
            model.addAttribute("proveedores", usuarioService.findAllProveedores()); // crea este método si no existe
        }
        return "calificaciones/form";
    }


    /** NUEVO: abrir form desde una solicitud finalizada */
    @GetMapping("/nueva/solicitud/{solicitudId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public String nuevaDesdeSolicitud(@PathVariable Long solicitudId,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      RedirectAttributes ra,
                                      Model model) {

        if (user == null) return "redirect:/auth/login?continue=/calificaciones/nueva/solicitud/" + solicitudId;

        SolicitudServicioEntity ss = solicitudServicioService.findById(solicitudId);
        if (ss == null || !ss.getCliente().getIdUsuario().equals(user.getUser().getIdUsuario())) {
            ra.addFlashAttribute("error", "Solicitud inválida.");
            return "redirect:/solicitud/historial";
        }
        if (!"FINALIZADO".equalsIgnoreCase(ss.getEstado())) {
            ra.addFlashAttribute("error", "Solo puedes calificar servicios finalizados.");
            return "redirect:/solicitud/historial";
        }
        if (ss.getServicio() == null || ss.getServicio().getProveedor() == null) {
            ra.addFlashAttribute("error", "Servicio sin proveedor asignado.");
            return "redirect:/solicitud/historial";
        }

        CalificacionCreateDTO dto = new CalificacionCreateDTO();
        dto.setServicioId(ss.getServicio().getIdServicio());
        dto.setProveedorId(ss.getServicio().getProveedor().getIdUsuario());
        // dto.setPuntuacion(5); // opcional

        model.addAttribute("title", "Nueva calificación");
        model.addAttribute("calificacion", dto);
        model.addAttribute("servicioNombre", ss.getServicio().getNombre());
        model.addAttribute("proveedorNombre", ss.getServicio().getProveedor().getNombreUsuario());

        return "calificaciones/form";
    }

    /** CREAR (upsert) */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public String crearDesdeForm(@Valid @ModelAttribute("calificacion") CalificacionCreateDTO dto,
                                 BindingResult br,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes ra,
                                 Model model) {

        if (user == null) {
            return "redirect:/auth/login?continue=/calificaciones/nueva";
        }

        if (br.hasErrors()) {
            model.addAttribute("title", "Nueva calificación");
            return "calificaciones/form";
        }

        try {
            calificacionService.crear(user.getUser().getIdUsuario(), dto);
            ra.addFlashAttribute("ok", "¡Gracias por tu calificación!");
            return "redirect:/calificaciones";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            br.reject("business.error", "Ya tienes una calificación para ese proveedor/servicio. Puedes editarla.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            br.reject("business.error", ex.getMessage());
        }

        model.addAttribute("title", "Nueva calificación");
        return "calificaciones/form";
    }
}