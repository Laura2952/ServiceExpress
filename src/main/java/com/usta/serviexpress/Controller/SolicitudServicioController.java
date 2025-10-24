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

@Controller
@RequestMapping("/solicitud")
public class SolicitudServicioController {

    // ======= IMPORTANTE: nombres EXACTOS de vistas (Linux es case-sensitive) =======
    private static final String VIEW_HISTORIAL_CLIENTE = "Solicitud/historialServicios"; // <-- tu archivo
    private static final String VIEW_HISTORIAL_PROV   = "Solicitud/serviciosSolicitados";
    private static final String VIEW_DETALLE          = "Solicitud/detalleSolicitud";

    @Autowired private ServicioService servicioService;
    @Autowired private SolicitudServicioService solicitudServicioService;

    @GetMapping("/crear/{id}")
    public String mostrarFormulario(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";
        ServicioEntity servicio = servicioService.findById(id);
        if (servicio == null) return "redirect:/servicio";
        model.addAttribute("servicio", servicio);
        return "Solicitud/solicitudServicio";
    }

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

    // ================== HISTORIAL (rol-aware) ==================
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
            return VIEW_HISTORIAL_CLIENTE; // <-- coincide con tu plantilla: Solicitud/HistorialServicios.html
        }
    }

    // ================== LISTAR PROVEEDOR ==================
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
        return VIEW_HISTORIAL_PROV; // Solicitud/serviciosSolicitados.html
    }

    // ================== PAGAR (cliente) ==================
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

    @GetMapping("/pagar/{id}")
    public String pagarAtajo(@PathVariable Long id) {
        return "redirect:/solicitud/pagar/redir/" + id;
    }

    // ================== CANCELAR (cliente) ==================
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

    // ================== DETALLE ==================
    @GetMapping("/detalle/{id}")
    public String detalleSolicitud(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (usuario == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/historial?error=No se pudo cargar la información";

        String rol = usuario.getRol() != null ? usuario.getRol().getRol() : "";
        boolean puedeVer = false;

        if ("ADMIN".equalsIgnoreCase(rol)) {
            puedeVer = true;
        } else if (solicitud.getCliente() != null && solicitud.getCliente().getIdUsuario().equals(usuario.getIdUsuario())) {
            puedeVer = true;
        } else if (solicitud.getServicio() != null
                && solicitud.getServicio().getProveedor() != null
                && solicitud.getServicio().getProveedor().getIdUsuario().equals(usuario.getIdUsuario())) {
            puedeVer = true;
        }

        if (!puedeVer) return "redirect:/solicitud/historial?error=No autorizado";

        model.addAttribute("solicitud", solicitud);
        return VIEW_DETALLE;
    }

    // ================== CAMBIAR ESTADO (PROVEEDOR) ==================
    @PostMapping("/proveedor/estado/{id}")
    public String cambiarEstadoSolicitud(@PathVariable Long id,
                                         @RequestParam String estado,
                                         @RequestParam(required = false)
                                         @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                         java.time.LocalDate fechaEstimada,
                                         jakarta.servlet.http.HttpSession session) {
        UsuarioEntity proveedor = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (proveedor == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/proveedor/listar?error=Solicitud no encontrada";

        if (solicitud.getServicio() == null || solicitud.getServicio().getProveedor() == null) {
            return "redirect:/solicitud/proveedor/listar?error=Solicitud sin proveedor asociado";
        }
        if (!solicitud.getServicio().getProveedor().getIdUsuario().equals(proveedor.getIdUsuario())) {
            return "redirect:/solicitud/proveedor/listar?error=No autorizado";
        }

        solicitud.setEstado(estado);
        if (fechaEstimada != null) solicitud.setFechaEstimada(fechaEstimada);
        solicitudServicioService.save(solicitud);

        return "redirect:/solicitud/proveedor/listar?success=Actualizado";
    }

    // ================== CAMBIAR ESTADO (ADMIN) ==================
    @PostMapping("/admin/estado/{id}")
    public String cambiarEstadoAdmin(@PathVariable Long id,
                                     @RequestParam String estado,
                                     HttpSession session) {
        UsuarioEntity admin = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (admin == null) return "redirect:/auth/login";
        if (admin.getRol() == null || !"ADMIN".equalsIgnoreCase(admin.getRol().getRol())) {
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

    // ================== CAMBIAR ESTADO (CLIENTE) ==================
    @PostMapping("/cliente/estado/{id}")
    public String cambiarEstadoCliente(@PathVariable Long id,
                                       @RequestParam String estado,
                                       @RequestParam(required = false) String fechaEstimada,
                                       HttpSession session) {
        UsuarioEntity cliente = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (cliente == null) return "redirect:/auth/login";

        SolicitudServicioEntity solicitud = solicitudServicioService.findById(id);
        if (solicitud == null) return "redirect:/solicitud/historial?error=Solicitud no encontrada";
        if (solicitud.getCliente() == null || !solicitud.getCliente().getIdUsuario().equals(cliente.getIdUsuario())) {
            return "redirect:/solicitud/historial?error=No autorizado";
        }
        if (!"PAGO_ACEPTADO".equalsIgnoreCase(solicitud.getEstado())
                && !"EN PROCESO".equalsIgnoreCase(solicitud.getEstado())) {
            return "redirect:/solicitud/historial?error=Estado actual no permite edición";
        }

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