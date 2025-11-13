package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ProveedorController
 *
 * Purpose:
 * - Manages provider (PROVEEDOR) actions such as publishing services, accepting requests,
 *   updating availability, viewing pending requests, viewing service history, and managing rates.
 * - Serves HTML views under "/proveedor" path for provider-related operations.
 *
 * Important notes:
 * - All methods assume the existence of a valid UsuarioEntity with role "PROVEEDOR".
 * - Methods rely on session attributes or path variables for identifying the current provider.
 * - RedirectAttributes are used to pass success/error messages to the views.
 */
@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    @Autowired private UsuarioService usuarioService; // Service for provider/user management
    @Autowired private ServicioService servicioService; // Service for service management

    /**
     * Provider landing page for services.
     * URL: GET /proveedor/servicios
     *
     * Parameters:
     * - session: HttpSession containing logged-in user.
     * - model: Spring Model to pass data to the view.
     *
     * Returns:
     * - Redirects to login if no session user is present.
     * - Otherwise, renders "Servicio/proveedor/listarServicios.html" with the provider's services.
     *
     * Notes:
     * - Loads historical services of the logged-in provider for display on the landing page.
     */
    @GetMapping("/servicios")
    public String landingServicios(HttpSession session, Model model) {
        UsuarioEntity u = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (u == null) return "redirect:/auth/login";

        List<ServicioEntity> servicios = servicioService.findHistorialByProveedor(u.getIdUsuario());
        model.addAttribute("servicios", servicios);
        return "Servicio/proveedor/listarServicios";
    }

    /**
     * Show form to publish a new service.
     * URL: GET /proveedor/{idProveedor}/publicarServicio
     *
     * Parameters:
     * - idProveedor: ID of the provider publishing the service.
     * - model: Spring Model to pass a new ServicioEntity and provider ID to the view.
     *
     * Returns:
     * - Renders "Proveedores/publicarServicio.html".
     */
    @GetMapping("/{idProveedor}/publicarServicio")
    public String publicarServicio(@PathVariable Long idProveedor, Model model) {
        model.addAttribute("idProveedor", idProveedor);
        model.addAttribute("servicio", new ServicioEntity());
        return "Proveedores/publicarServicio";
    }

    /**
     * Publish a new service.
     * URL: POST /proveedor/{idProveedor}/publicarServicio
     *
     * Parameters:
     * - idProveedor: ID of the provider publishing the service.
     * - servicio: ServicioEntity populated from form submission.
     * - ra: RedirectAttributes to pass feedback messages to the view.
     *
     * Returns:
     * - Redirects to provider landing page after saving the service.
     * - Redirects to login with error if provider is invalid.
     *
     * Notes:
     * - Ensures the user has role "PROVEEDOR" before saving.
     */
    @PostMapping("/{idProveedor}/publicarServicio")
    public String publicarServicio(@PathVariable("idProveedor") Long idProveedor,
                                   @ModelAttribute ServicioEntity servicio,
                                   RedirectAttributes ra) {
        UsuarioEntity proveedor = usuarioService.findById(idProveedor);
        if (proveedor == null || proveedor.getRol() == null ||
                ! "PROVEEDOR".equalsIgnoreCase(proveedor.getRol().getRol())) {
            ra.addFlashAttribute("error", "Proveedor no encontrado o inválido");
            return "redirect:/auth/login";
        }
        servicio.setProveedor(proveedor);
        servicioService.save(servicio);
        ra.addFlashAttribute("success", "Servicio publicado correctamente");
        return "redirect:/proveedor/servicios";
    }

    /**
     * Accept a service request.
     * URL: POST /proveedor/{idProveedor}/aceptarSolicitud/{idServicio}
     *
     * Parameters:
     * - idProveedor: ID of the provider accepting the service.
     * - idServicio: ID of the service request to accept.
     * - ra: RedirectAttributes to pass feedback messages to the view.
     *
     * Returns:
     * - Redirects back to pending requests page.
     *
     * Notes:
     * - Sets the service status to ACEPTADA.
     * - Displays error if service not found.
     */
    @PostMapping("/{idProveedor}/aceptarSolicitud/{idServicio}")
    public String aceptarSolicitud(@PathVariable("idProveedor") Long idProveedor,
                                   @PathVariable("idServicio") Long idServicio,
                                   RedirectAttributes ra) {
        ServicioEntity servicio = servicioService.findById(idServicio);
        if (servicio == null) {
            ra.addFlashAttribute("error", "Servicio no encontrado");
            return "redirect:/proveedor/" + idProveedor + "/solicitudesPendientes";
        }
        servicio.setEstado(ServicioEntity.EstadoServicio.ACEPTADA);
        servicioService.save(servicio);
        ra.addFlashAttribute("success", "Solicitud aceptada");
        return "redirect:/proveedor/" + idProveedor + "/solicitudesPendientes";
    }

    /**
     * Update provider's availability.
     * URL: POST /proveedor/{idProveedor}/actualizarDisponibilidad
     *
     * Parameters:
     * - idProveedor: ID of the provider updating availability.
     * - disponibilidad: boolean indicating provider's availability.
     * - ra: RedirectAttributes to pass feedback messages.
     *
     * Returns:
     * - Redirects to provider landing page.
     *
     * Notes:
     * - Only updates if the user has role "PROVEEDOR".
     */
    @PostMapping("/{idProveedor}/actualizarDisponibilidad")
    public String actualizarDisponibilidad(@PathVariable("idProveedor") Long idProveedor,
                                           @RequestParam("disponibilidad") boolean disponibilidad,
                                           RedirectAttributes ra) {
        UsuarioEntity proveedor = usuarioService.findById(idProveedor);
        if (proveedor != null && proveedor.getRol() != null &&
                "PROVEEDOR".equalsIgnoreCase(proveedor.getRol().getRol())) {
            proveedor.setDisponibilidad(disponibilidad);
            usuarioService.save(proveedor);
            ra.addFlashAttribute("success", "Disponibilidad actualizada");
        } else {
            ra.addFlashAttribute("error", "Proveedor no encontrado");
        }
        return "redirect:/proveedor/servicios";
    }

    /**
     * View pending service requests for a provider.
     * URL: GET /proveedor/{idProveedor}/solicitudesPendientes
     *
     * Parameters:
     * - idProveedor: ID of the provider.
     * - model: Spring Model to pass the list of pending services.
     *
     * Returns:
     * - Renders "Proveedores/solicitudPendiente.html".
     */
    @GetMapping("/{idProveedor}/solicitudesPendientes")
    public String verSolicitudesPendientes(@PathVariable("idProveedor") Long idProveedor, Model model) {
        List<ServicioEntity> pendientes = servicioService.findPendientesByProveedor(idProveedor);
        model.addAttribute("solicitudesPendientes", pendientes);
        return "Proveedores/solicitudPendiente";
    }

    /**
     * View provider's service history.
     * URL: GET /proveedor/{idProveedor}/historialServicios
     *
     * Parameters:
     * - idProveedor: ID of the provider.
     * - model: Spring Model to pass historical services to the view.
     *
     * Returns:
     * - Renders "Proveedores/historialServicio.html".
     */
    @GetMapping("/{idProveedor}/historialServicios")
    public String verHistorialServicio(@PathVariable("idProveedor") Long idProveedor, Model model) {
        List<ServicioEntity> historial = servicioService.findHistorialByProveedor(idProveedor);
        model.addAttribute("historialServicios", historial);
        return "Proveedores/historialServicio";
    }

    /**
     * Update provider's service rate/tariff.
     * URL: POST /proveedor/{idProveedor}/gestionarTarifas
     *
     * Parameters:
     * - idProveedor: ID of the provider.
     * - tarifa: double representing the new rate.
     * - ra: RedirectAttributes to pass feedback messages.
     *
     * Returns:
     * - Redirects to provider landing page.
     *
     * Notes:
     * - Only updates if the user has role "PROVEEDOR".
     */
    @PostMapping("/{idProveedor}/gestionarTarifas")
    public String gestionarTarifas(@PathVariable("idProveedor") Long idProveedor,
                                   @RequestParam("tarifa") double tarifa,
                                   RedirectAttributes ra) {
        UsuarioEntity proveedor = usuarioService.findById(idProveedor);
        if (proveedor != null && proveedor.getRol() != null &&
                "PROVEEDOR".equalsIgnoreCase(proveedor.getRol().getRol())) {
            proveedor.setTarifa(tarifa);
            usuarioService.save(proveedor);
            ra.addFlashAttribute("success", "Tarifa actualizada");
        } else {
            ra.addFlashAttribute("error", "Proveedor no encontrado");
        }
        return "redirect:/proveedor/servicios";
    }
}

/*
Summary (Technical Note):
ProveedorController manages all provider-related operations in the Serviexpress platform. 
It handles provider service publishing, accepting client requests, updating availability, 
viewing pending and historical services, and managing rates. It uses session data or path 
variables to identify the provider and ensures that only users with role "PROVEEDOR" can 
perform actions. Feedback messages are passed to views using RedirectAttributes, and views 
are rendered as Thymeleaf templates corresponding to provider functionalities.
*/
