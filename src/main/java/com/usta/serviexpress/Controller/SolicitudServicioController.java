// src/main/java/com/usta/serviexpress/Controller/SolicitudServicioController.java
package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.SolicitudServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.SolicitudServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * SolicitudServicioController
 *
 * Purpose:
 * - Handles web requests related to creating, managing, and tracking service requests (solicitudes).
 * - Supports role-aware views and actions (CLIENTE, PROVEEDOR, ADMIN).
 * - Integrates with session management to check logged-in user roles.
 *
 * Base path: /solicitud
 *
 * Important:
 * - View names are case-sensitive on Linux; ensure template file names match the constants exactly.
 * - Uses Thymeleaf or similar template engine for HTML rendering.
 */
@Controller
@RequestMapping("/solicitud")
public class SolicitudServicioController {

    // ====== VIEW CONSTANTS ======
    // Exact paths to HTML templates (case-sensitive!)
    private static final String VIEW_HISTORIAL_CLIENTE = "Solicitud/historialServicios";
    private static final String VIEW_HISTORIAL_PROV   = "Solicitud/serviciosSolicitados";
    private static final String VIEW_DETALLE          = "Solicitud/detalleSolicitud";

    @Autowired private ServicioService servicioService; // Handles ServicioEntity logic
    @Autowired private SolicitudServicioService solicitudServicioService; // Handles SolicitudServicioEntity logic

    // ================== FORMULARIO DE CREACION ==================
    /**
     * GET /solicitud/crear/{id}
     * Display form to create a new service request for a given service ID.
     * 
     * Parameters:
     * - id: Service ID to request
     * 
     * Behavior:
     * - Checks if user is logged in (CLIENTE role).
     * - If service exists, loads it into the model for the form.
     * - Redirects to login or service list if checks fail.
     */
    @GetMapping("/crear/{id}")
    public String mostrarFormulario(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        ServicioEntity servicio = servicioService.findById(id);
        if (servicio == null) return "redirect:/servicio";

        model.addAttribute("servicio", servicio);
        return "Solicitud/solicitudServicio";
    }

    // ================== GUARDAR NUEVA SOLICITUD ==================
    /**
     * POST /solicitud/guardar
     * Handles saving a new service request from the form submission.
     * 
     * Parameters:
     * - idServicio, detalles, direccionEntrega: Form data
     * 
     * Behavior:
     * - Checks user session
     * - Validates that service exists and is AVAILABLE
     * - Creates SolicitudServicioEntity, sets initial status to PENDIENTE
     * - Saves it to database
     */
    @PostMapping("/guardar")
    public String guardarSolicitud(@RequestParam Long idServicio,
                                   @RequestParam String detalles,
                                   @RequestParam String direccionEntrega,
                                   HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        ServicioEntity servicio = servicioService.findById(idServicio);
        if (servicio == null || servicio.getEstado() != ServicioEntity.EstadoServicio.DISPONIBLE) {
            return "redirect:/servicio";
        }

        SolicitudServicioEntity solicitud = new SolicitudServicioEntity();
        solicitud.setServicio(servicio);
        solicitud.setCliente(cliente);
        solicitud.setFechaSolicitud(LocalDate.now());
        solicitud.setEstado("PENDIENTE");
        solicitud.setDetalles(detalles);
        solicitud.setDireccionEntrega(direccionEntrega);

        solicitudServicioService.save(solicitud);
        return "redirect:/servicio?success=Solicitud realizada con éxito";
    }

    // ================== HISTORIAL SOLICITUDES ==================
    /**
     * GET /solicitud/historial
     * Displays a role-aware history of service requests.
     * 
     * Behavior:
     * - Detects role (CLIENTE, PROVEEDOR, ADMIN)
     * - Loads relevant requests for the user
     * - Adds flags to model to handle view logic
     */
    @GetMapping("/historial")
    public String historialSolicitudes(Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        String rol = usuario.getRol() != null ? usuario.getRol().getRol() : "";

        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(rol));
        model.addAttribute("isProveedor", "PROVEEDOR".equalsIgnoreCase(rol));
        model.addAttribute("isCliente", "CLIENTE".equalsIgnoreCase(rol));

        if ("ADMIN".equalsIgnoreCase(rol)) {
            List<SolicitudServicioEntity> todas = solicitudServicioService.findAll();
            model.addAttribute("solicitudesProveedor", todas);
            return VIEW_HISTORIAL_PROV;
        } else if ("PROVEEDOR".equalsIgnoreCase(rol)) {
            List<SolicitudServicioEntity> proveedorSolicitudes = solicitudServicioService.findByProveedor(usuario);
            model.addAttribute("solicitudesProveedor", proveedorSolicitudes);
            return VIEW_HISTORIAL_PROV;
        } else {
            List<SolicitudServicioEntity> clienteSolicitudes = solicitudServicioService.findByCliente(usuario);
            model.addAttribute("solicitudes", clienteSolicitudes);
            return VIEW_HISTORIAL_CLIENTE;
        }
    }

    // ================== LISTAR SOLICITUDES PARA PROVEEDOR ==================
    @GetMapping("/proveedor/listar")
    public String listarSolicitudesProveedor(Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        String rol = usuario.getRol() != null ? usuario.getRol().getRol() : "";

        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(rol));
        model.addAttribute("isProveedor", "PROVEEDOR".equalsIgnoreCase(rol));
        model.addAttribute("isCliente", "CLIENTE".equalsIgnoreCase(rol));

        if ("ADMIN".equalsIgnoreCase(rol)) {
            model.addAttribute("solicitudesProveedor", solicitudServicioService.findAll());
        } else {
            model.addAttribute("solicitudesProveedor", solicitudServicioService.findByProveedor(usuario));
        }
        return VIEW_HISTORIAL_PROV;
    }

    // ================== PAGAR SOLICITUD (CLIENTE) ==================
    @GetMapping("/pagar/redir/{id}")
    public String pagarSolicitud(@PathVariable Long id, HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud != null && solicitud.getCliente() != null
                && solicitud.getCliente().getIdUsuario().equals(cliente.getIdUsuario())) {
            solicitud.setEstado("PAGO_EN_PROCESO");
            solicitudServicioService.save(solicitud);
            return "redirect:/checkout/wompi/" + id;
        }
        return "redirect:/solicitud/historial?error=No se pudo procesar el pago";
    }

    // Shortcut to redirect to pagar/redir
    @GetMapping("/pagar/{id}")
    public String pagarAtajo(@PathVariable Long id) {
        return "redirect:/solicitud/pagar/redir/" + id;
    }

    // ================== CANCELAR SOLICITUD (CLIENTE) ==================
    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable Long id, HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud != null && solicitud.getCliente() != null
                && solicitud.getCliente().getIdUsuario().equals(cliente.getIdUsuario())) {

            ServicioEntity servicio = solicitud.getServicio();
            if (servicio != null) {
                servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);
                servicio.setCliente(null);
                servicioService.save(servicio);
            }

            solicitudServicioService.deleteById(id);
            return "redirect:/solicitud/historial?success=Solicitud cancelada con éxito";
        }
        return "redirect:/solicitud/historial?error=No se pudo cancelar la solicitud";
    }

    // ================== DETALLE SOLICITUD ==================
    @GetMapping("/detalle/{id}")
    public String detalleSolicitud(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/historial?error=No se pudo cargar la información";

        String rol = usuario.getRol() != null ? usuario.getRol().getRol() : "";
        boolean puedeVer = "ADMIN".equalsIgnoreCase(rol)
                || (solicitud.getCliente() != null && solicitud.getCliente().getIdUsuario().equals(usuario.getIdUsuario()))
                || (solicitud.getServicio() != null && solicitud.getServicio().getProveedor() != null
                    && solicitud.getServicio().getProveedor().getIdUsuario().equals(usuario.getIdUsuario()));

        if (!puedeVer) return "redirect:/solicitud/historial?error=No autorizado";

        model.addAttribute("solicitud", solicitud);
        return VIEW_DETALLE;
    }

    // ================== CAMBIAR ESTADO SOLICITUD (PROVEEDOR) ==================
    @PostMapping("/proveedor/estado/{id}")
    public String cambiarEstadoSolicitud(@PathVariable Long id,
                                         @RequestParam String estado,
                                         @RequestParam(required = false)
                                         @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                         LocalDate fechaEstimada,
                                         HttpSession session) {
        UsuarioEntity proveedor = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (proveedor == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/proveedor/listar?error=Solicitud no encontrada";
        if (solicitud.getServicio() == null || solicitud.getServicio().getProveedor() == null
                || !solicitud.getServicio().getProveedor().getIdUsuario().equals(proveedor.getIdUsuario())) {
            return "redirect:/solicitud/proveedor/listar?error=No autorizado";
        }

        solicitud.setEstado(estado);
        if (fechaEstimada != null) solicitud.setFechaEstimada(fechaEstimada);
        solicitudServicioService.save(solicitud);

        return "redirect:/solicitud/proveedor/listar?success=Actualizado";
    }

    // ================== CAMBIAR ESTADO SOLICITUD (ADMIN) ==================
    @PostMapping("/admin/estado/{id}")
    public String cambiarEstadoAdmin(@PathVariable Long id,
                                     @RequestParam String estado,
                                     HttpSession session) {
        UsuarioEntity admin = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (admin == null || admin.getRol() == null || !"ADMIN".equalsIgnoreCase(admin.getRol().getRol())) {
            return "redirect:/solicitud/historial?error=No autorizado";
        }

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/historial?error=Solicitud no encontrada";
        if (!"PAGO_EN_PROCESO".equalsIgnoreCase(solicitud.getEstado())) {
            return "redirect:/solicitud/historial?error=Estado no válido";
        }

        solicitud.setEstado(estado);
        solicitudServicioService.save(solicitud);
        return "redirect:/solicitud/historial?success=Estado actualizado correctamente";
    }

    // ================== CAMBIAR ESTADO SOLICITUD (CLIENTE) ==================
    @PostMapping("/cliente/estado/{id}")
    public String cambiarEstadoCliente(@PathVariable Long id,
                                       @RequestParam String estado,
                                       @RequestParam(required = false) String fechaEstimada,
                                       HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null || solicitud.getCliente() == null
                || !solicitud.getCliente().getIdUsuario().equals(cliente.getIdUsuario())) {
            return "redirect:/solicitud/historial?error=No autorizado o solicitud no encontrada";
        }

        if (!"PAGO_ACEPTADO".equalsIgnoreCase(solicitud.getEstado())
                && !"EN PROCESO".equalsIgnoreCase(solicitud.getEstado())) {
            return "redirect:/solicitud/historial?error=Estado actual no permite edición";
        }

        // Parse estimated date if provided
        if (fechaEstimada != null && !fechaEstimada.isBlank()) {
            try {
                solicitud.setFechaEstimada(LocalDate.parse(fechaEstimada));
            } catch (DateTimeParseException ex) {
                return "redirect:/solicitud/historial?error=Fecha estimada inválida";
            }
        } else {
            solicitud.setFechaEstimada(null);
        }

        solicitud.setEstado(estado);
        solicitudServicioService.save(solicitud);
        return "redirect:/solicitud/historial?success=Cambios guardados";
    }
}

/*
Summary:
- Handles creation, viewing, payment, cancellation, and state changes of service requests.
- Role-aware: CLIENTE, PROVEEDOR, ADMIN.
- Uses session to verify logged-in users and their permissions.
- Returns HTML views (Thymeleaf or similar) with relevant model data.
- Performs basic validation, error handling, and redirection for unauthorized access.
*/
