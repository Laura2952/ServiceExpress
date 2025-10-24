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

@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ServicioService servicioService;

    /* ========= LANDING DEL PROVEEDOR =========
       Resuelve /proveedor/servicios para evitar el 404 al iniciar sesión */
    @GetMapping("/servicios")
    public String landingServicios(HttpSession session, Model model) {
        UsuarioEntity u = (UsuarioEntity) session.getAttribute("usuarioSesion");
        if (u == null) return "redirect:/auth/login";

        // carga algo útil para la pantalla de inicio del proveedor
        List<ServicioEntity> servicios = servicioService.findHistorialByProveedor(u.getIdUsuario());
        model.addAttribute("servicios", servicios);
        return "Servicio/proveedor/listarServicios"; // coincide con templates/Servicio/proveedor/listarServicios.html
    }

    // Mostrar formulario para publicar servicio
    @GetMapping("/{idProveedor}/publicarServicio")
    public String publicarServicio(@PathVariable Long idProveedor, Model model) {
        model.addAttribute("idProveedor", idProveedor);
        model.addAttribute("servicio", new ServicioEntity());
        return "Proveedores/publicarServicio"; // coincide con templates/Proveedores/publicarServicio.html
    }

    // Publicar un nuevo servicio
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

    // Aceptar una solicitud de servicio
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

    // Actualizar disponibilidad del proveedor
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

    // Ver solicitudes pendientes
    @GetMapping("/{idProveedor}/solicitudesPendientes")
    public String verSolicitudesPendientes(@PathVariable("idProveedor") Long idProveedor, Model model) {
        List<ServicioEntity> pendientes = servicioService.findPendientesByProveedor(idProveedor);
        model.addAttribute("solicitudesPendientes", pendientes);
        return "Proveedores/solicitudPendiente"; // coincide con templates/Proveedores/solicitudPendiente.html
    }

    // Ver historial de servicios del proveedor
    @GetMapping("/{idProveedor}/historialServicios")
    public String verHistorialServicio(@PathVariable("idProveedor") Long idProveedor, Model model) {
        List<ServicioEntity> historial = servicioService.findHistorialByProveedor(idProveedor);
        model.addAttribute("historialServicios", historial);
        return "Proveedores/historialServicio"; // coincide con templates/Proveedores/historialServicio.html
    }

    // Gestionar tarifas
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