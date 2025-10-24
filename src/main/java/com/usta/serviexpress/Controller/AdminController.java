package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.ServicioEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.RolService;
import com.usta.serviexpress.Service.ServicioService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/Admins")
public class AdminController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ServicioService servicioService;
    @Autowired private RolService rolService;

    /* ==================== USUARIOS ==================== */

    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        model.addAttribute("title", "Gestión de Usuarios (Admin)");
        model.addAttribute("urlRegister", "/Admins/usuarios/crear");
        List<UsuarioEntity> lista = usuarioService.findAll();
        lista.sort(Comparator.comparing(UsuarioEntity::getIdUsuario));
        model.addAttribute("Usuarios", lista);
        return "Admins/gestionarUsuario";
    }

    @GetMapping("/usuarios/crear")
    public String crearUsuarioUsuariosCrear(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("roles", rolService.findAll());
        return "Admins/crearUsuario";
    }

    @PostMapping("/usuarios/crear")
    public String crear(@Valid @ModelAttribute("usuario") UsuarioEntity usuario,
                        BindingResult result,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "Admins/crearUsuario";
        }
        usuarioService.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/Admins/usuarios";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioEntity usuario = usuarioService.findById(idUsuario);
        model.addAttribute("title", "Editar Usuario (Admin)");
        model.addAttribute("usuarioEdit", usuario);
        model.addAttribute("roles", rolService.findAll());
        return "Admins/editarUsuario";
    }

    @PostMapping("/usuarios/{id}/editar")
    public String editarUsuario(@ModelAttribute("usuarioEdit") UsuarioEntity usuario,
                                @PathVariable("id") Long idUsuario,
                                BindingResult result,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "Admins/editarUsuario";
        }
        UsuarioEntity existente = usuarioService.findById(idUsuario);
        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setRol(usuario.getRol());
        existente.setClave(usuario.getClave());
        existente.setCorreo(usuario.getCorreo());
        existente.setTelefono(usuario.getTelefono());
        existente.setCiudad(usuario.getCiudad());
        usuarioService.save(existente);

        ra.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
        return "redirect:/Admins/usuarios";
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable("id") long id, RedirectAttributes ra) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usernameLogeado = auth.getName();
            UsuarioEntity usuarioLogeado = usuarioService.findByCorreo(usernameLogeado);

            if (usuarioLogeado != null && usuarioLogeado.getIdUsuario() == id) {
                ra.addFlashAttribute("error", "No puedes eliminar tu propia cuenta mientras estás logeado.");
                return "redirect:/Admins/usuarios";
            }

            usuarioService.deleteById(id);
            ra.addFlashAttribute("success", "Usuario eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/Admins/usuarios";
    }

    /* ==================== SERVICIOS ==================== */

    @GetMapping("/servicios")
    public String gestionarServicios(Model model) {
        model.addAttribute("title", "Gestión de Servicios (Admin)");
        model.addAttribute("urlRegister", "/Admins/servicios/crear");
        List<ServicioEntity> lista = servicioService.findAll();
        lista.sort(Comparator.comparing(ServicioEntity::getIdServicio));
        model.addAttribute("Servicios", lista);
        return "Admins/gestionarServicio";
    }

    @GetMapping("/servicios/crear")
    public String crearServicio(Model model) {
        model.addAttribute("title", "Registrar Servicio (Admin)");
        model.addAttribute("servicio", new ServicioEntity());
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        model.addAttribute("proveedores", usuarioService.findAllProveedores());
        return "Admins/crearServicio";
    }

    @PostMapping("/servicios/crear")
    public String crearServicio(@Valid @ModelAttribute("servicio") ServicioEntity servicio,
                                BindingResult result,
                                @RequestParam("proveedorNombre") String proveedorNombre,
                                RedirectAttributes ra,
                                Model model) {

        if (result.hasErrors()) {
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            return "Admins/crearServicio";
        }

        String nombreBuscado = proveedorNombre == null ? "" : proveedorNombre.trim();
        if (nombreBuscado.isEmpty()) {
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            model.addAttribute("error", "Debes seleccionar un proveedor.");
            return "Admins/crearServicio";
        }

        UsuarioEntity proveedor = usuarioService.findAllProveedores().stream()
                .filter(p -> p.getNombreUsuario() != null
                        && p.getNombreUsuario().trim().equalsIgnoreCase(nombreBuscado))
                .findFirst()
                .orElse(null);

        if (proveedor == null) {
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            model.addAttribute("error", "Proveedor inválido o no encontrado.");
            return "Admins/crearServicio";
        }

        // 🔹 Estado por defecto
        servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);

        servicio.setProveedor(proveedor);
        servicioService.save(servicio);
        ra.addFlashAttribute("mensajeExito", "Servicio creado correctamente");
        return "redirect:/Admins/servicios";
    }

    @GetMapping("/servicios/{id}/editar")
    public String editarServicio(@PathVariable("id") Long idServicio, Model model) {
        ServicioEntity servicio = servicioService.findById(idServicio);
        model.addAttribute("title", "Editar Servicio (Admin)");
        model.addAttribute("servicio", servicio);
        model.addAttribute("servicioEdit", servicio);
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        return "Admins/editarServicio";
    }

    @PostMapping("/servicios/{id}/editar")
    public String editarServicio(@ModelAttribute("servicioEdit") ServicioEntity servicio,
                                 @PathVariable("id") Long idServicio,
                                 BindingResult result,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "Admins/editarServicio";
        }
        ServicioEntity existente = servicioService.findById(idServicio);
        existente.setNombre(servicio.getNombre());
        existente.setDescripcion(servicio.getDescripcion());
        existente.setPrecio(servicio.getPrecio());
        existente.setEstado(servicio.getEstado());
        servicioService.save(existente);

        ra.addFlashAttribute("mensajeExito", "Servicio actualizado correctamente");
        return "redirect:/Admins/servicios";
    }

    @PostMapping("/servicios/{id}/eliminar")
    public String eliminarServicio(@PathVariable("id") long id, RedirectAttributes ra) {
        try {
            servicioService.deleteById(id);
            ra.addFlashAttribute("success", "Servicio eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/Admins/servicios";
    }

    // ==================== HISTORIAL DE SERVICIOS (ADMIN) ====================
    @GetMapping("/servicios/historial")
    public String historialServiciosAdmin(Model model) {
        model.addAttribute("title", "Historial de Servicios");
        List<ServicioEntity> lista = servicioService.findAll();
        lista.sort(Comparator.comparing(ServicioEntity::getIdServicio).reversed());
        model.addAttribute("solicitudes", lista); // 🔹 CAMBIO: ahora siempre se llama "solicitudes"
        return "Solicitud/historialServicios";
    }
}
