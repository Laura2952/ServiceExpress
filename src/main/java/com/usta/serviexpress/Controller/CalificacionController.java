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

/**
 * CalificacionController
 *
 * Purpose:
 * - Handles the CRUD and workflow related to service ratings ("calificaciones") within ServiExpress.
 * - Allows users (particularly clients) to create, view, and filter service ratings.
 * - Integrates with multiple services: CalificacionService, SolicitudServicioService, ServicioService, and UsuarioService.
 *
 * Security:
 * - Access to rating creation routes is restricted to users with the CLIENTE role.
 * - Uses Spring Security’s @AuthenticationPrincipal for obtaining logged-in user details.
 *
 * Views:
 * - "calificaciones/lista" for listing ratings.
 * - "calificaciones/form" for rating creation.
 *
 * Notes:
 * - Routes are prefixed with "/calificaciones".
 * - Uses flash messages (RedirectAttributes) for user feedback on redirects.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;            // Service layer for CRUD operations on ratings.
    private final SolicitudServicioService solicitudServicioService;  // Handles service request entities.
    private final ServicioService servicioService;                    // Provides service catalog access.
    private final UsuarioService usuarioService;                      // Provides user-related data access.

    /**
     * Displays a list of ratings, optionally filtered by score.
     *
     * Route: GET /calificaciones
     *
     * @param rating Optional query parameter for filtering by specific score (1–5).
     * @param model  Spring Model to populate the view with attributes.
     * @return View "calificaciones/lista".
     *
     * Logic:
     * - If no rating is provided, all ratings are retrieved.
     * - Otherwise, filters by the provided score value.
     */
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

    /**
     * Displays the rating creation form (standalone access).
     *
     * Route: GET /calificaciones/nueva
     *
     * @param servicio  Optional service ID to preselect.
     * @param proveedor Optional provider ID to preselect.
     * @param model     Spring Model for the form.
     * @return View "calificaciones/form".
     *
     * Behavior:
     * - Initializes a new CalificacionCreateDTO with optional preselected service/provider.
     * - If no context is given, loads dropdowns for available services and providers.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long servicio,
                        @RequestParam(required = false) Long proveedor,
                        Model model) {

        CalificacionCreateDTO dto = new CalificacionCreateDTO();
        dto.setServicioId(servicio);
        dto.setProveedorId(proveedor);

        model.addAttribute("title", "Nueva calificación");
        model.addAttribute("calificacion", dto);

        // If no service/provider context provided, load options
        if (servicio == null && proveedor == null) {
            model.addAttribute("servicios", servicioService.findAll());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
        }

        return "calificaciones/form";
    }

    /**
     * Opens the rating form from a completed service request.
     *
     * Route: GET /calificaciones/nueva/solicitud/{solicitudId}
     * Access: Restricted to CLIENTE role.
     *
     * @param solicitudId ID of the completed service request to rate.
     * @param user        Authenticated client.
     * @param ra          RedirectAttributes for flash messages.
     * @param model       Model for passing data to the form.
     * @return Rating form view if validation passes, otherwise redirect to request history.
     *
     * Validation checks:
     * - The logged user must match the request’s client.
     * - The request must be in "FINALIZADO" (completed) state.
     * - The service must have an assigned provider.
     */
    @GetMapping("/nueva/solicitud/{solicitudId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public String nuevaDesdeSolicitud(@PathVariable Long solicitudId,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      RedirectAttributes ra,
                                      Model model) {

        // Ensure user is logged in; redirect to login if not
        if (user == null) return "redirect:/auth/login?continue=/calificaciones/nueva/solicitud/" + solicitudId;

        SolicitudServicioEntity ss = solicitudServicioService.findById(solicitudId);

        // Validate ownership and request existence
        if (ss == null || !ss.getCliente().getIdUsuario().equals(user.getUser().getIdUsuario())) {
            ra.addFlashAttribute("error", "Solicitud inválida.");
            return "redirect:/solicitud/historial";
        }

        // Only finalized service requests can be rated
        if (!"FINALIZADO".equalsIgnoreCase(ss.getEstado())) {
            ra.addFlashAttribute("error", "Solo puedes calificar servicios finalizados.");
            return "redirect:/solicitud/historial";
        }

        // Ensure service and provider exist
        if (ss.getServicio() == null || ss.getServicio().getProveedor() == null) {
            ra.addFlashAttribute("error", "Servicio sin proveedor asignado.");
            return "redirect:/solicitud/historial";
        }

        // Prepopulate rating form with related service and provider info
        CalificacionCreateDTO dto = new CalificacionCreateDTO();
        dto.setServicioId(ss.getServicio().getIdServicio());
        dto.setProveedorId(ss.getServicio().getProveedor().getIdUsuario());
        // dto.setPuntuacion(5); // Optional default score

        model.addAttribute("title", "Nueva calificación");
        model.addAttribute("calificacion", dto);
        model.addAttribute("servicioNombre", ss.getServicio().getNombre());
        model.addAttribute("proveedorNombre", ss.getServicio().getProveedor().getNombreUsuario());

        return "calificaciones/form";
    }

    /**
     * Processes a new rating submission from the form (create or upsert).
     *
     * Route: POST /calificaciones
     * Access: Restricted to CLIENTE role.
     *
     * @param dto   Rating creation DTO validated by Jakarta Validation.
     * @param br    BindingResult capturing validation errors.
     * @param user  Authenticated client (must be logged in).
     * @param ra    RedirectAttributes for success messages.
     * @param model Spring Model for redisplaying form on error.
     * @return Redirect to rating list upon success, or redisplay form if errors occur.
     *
     * Error handling:
     * - DataIntegrityViolationException: Duplicate rating for the same provider/service.
     * - IllegalArgumentException / IllegalStateException: Business rule violations.
     *
     * Notes:
     * - Calls calificacionService.crear() which handles persistence and business logic.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public String crearDesdeForm(@Valid @ModelAttribute("calificacion") CalificacionCreateDTO dto,
                                 BindingResult br,
                                 @AuthenticationPrincipal CustomUserDetails user,
                                 RedirectAttributes ra,
                                 Model model) {

        // Redirect to login if not authenticated
        if (user == null) {
            return "redirect:/auth/login?continue=/calificaciones/nueva";
        }

        // Return form with errors if validation failed
        if (br.hasErrors()) {
            model.addAttribute("title", "Nueva calificación");
            return "calificaciones/form";
        }

        try {
            // Create new rating
            calificacionService.crear(user.getUser().getIdUsuario(), dto);
            ra.addFlashAttribute("ok", "¡Gracias por tu calificación!");
            return "redirect:/calificaciones";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Handle duplicate ratings
            br.reject("business.error", "Ya tienes una calificación para ese proveedor/servicio. Puedes editarla.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Handle business validation errors
            br.reject("business.error", ex.getMessage());
        }

        // Return to form view with validation feedback
        model.addAttribute("title", "Nueva calificación");
        return "calificaciones/form";
    }
}

/*
Summary (Technical Note):
CalificacionController manages the user interface for creating and listing service ratings.
It enforces business rules such as allowing only clients to rate finalized service requests,
and ensures ratings are unique per provider/service pair. The controller integrates with
various service layers to fetch user, service, and request data. It supports optional filtering
by score, context-aware rating creation (from service requests), and secure session-based
user validation. Errors are handled gracefully with user-friendly feedback and flash messages.
*/
