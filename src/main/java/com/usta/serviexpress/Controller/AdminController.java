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

/**
 * AdminController
 * -----------------------------------------------------
 * This controller manages all administrative operations related to:
 *  - User management (CRUD for UsuarioEntity)
 *  - Service management (CRUD for ServicioEntity)
 *  - Viewing service history
 *
 * It is part of the Admin module of the application and provides routes
 * under the base URL "/Admins".
 *
 * Uses Spring MVC annotations to handle HTTP requests and render views.
 */
@Controller
@RequestMapping("/Admins")
public class AdminController {

    // ==================== DEPENDENCY INJECTIONS ====================

    @Autowired private UsuarioService usuarioService;  // Service for managing user entities
    @Autowired private ServicioService servicioService; // Service for managing service entities
    @Autowired private RolService rolService;           // Service for managing user roles


    /* ==================== USER MANAGEMENT ==================== */

    /**
     * Displays the list of all users for the admin.
     * @param model Model used to pass data to the Thymeleaf view.
     * @return Path to "gestionarUsuario" view.
     */
    @GetMapping("/usuarios")
    public String gestionarUsuarios(Model model) {
        model.addAttribute("title", "Gestión de Usuarios (Admin)");
        model.addAttribute("urlRegister", "/Admins/usuarios/crear");

        // Retrieve and sort all users by ID
        List<UsuarioEntity> lista = usuarioService.findAll();
        lista.sort(Comparator.comparing(UsuarioEntity::getIdUsuario));

        model.addAttribute("Usuarios", lista);
        return "Admins/gestionarUsuario";
    }

    /**
     * Displays the user creation form.
     */
    @GetMapping("/usuarios/crear")
    public String crearUsuarioUsuariosCrear(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("roles", rolService.findAll()); // Provide available roles
        return "Admins/crearUsuario";
    }

    /**
     * Handles the POST request to create a new user.
     * @param usuario The user entity populated from the form.
     * @param result Validation result for the user entity.
     * @param ra Redirect attributes used to send feedback messages.
     */
    @PostMapping("/usuarios/crear")
    public String crear(@Valid @ModelAttribute("usuario") UsuarioEntity usuario,
                        BindingResult result,
                        RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "Admins/crearUsuario"; // Validation errors, return to form
        }
        usuarioService.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/Admins/usuarios";
    }

    /**
     * Displays the edit form for a specific user.
     * @param idUsuario ID of the user to be edited.
     */
    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioEntity usuario = usuarioService.findById(idUsuario);
        model.addAttribute("title", "Editar Usuario (Admin)");
        model.addAttribute("usuarioEdit", usuario);
        model.addAttribute("roles", rolService.findAll());
        return "Admins/editarUsuario";
    }

    /**
     * Handles the POST request to update user information.
     * @param usuario Updated user data from form binding.
     * @param idUsuario User ID to update.
     */
    @PostMapping("/usuarios/{id}/editar")
    public String editarUsuario(@ModelAttribute("usuarioEdit") UsuarioEntity usuario,
                                @PathVariable("id") Long idUsuario,
                                BindingResult result,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "Admins/editarUsuario";
        }

        // Retrieve existing user and update fields manually
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

    /**
     * Deletes a user by ID. Prevents deleting the currently logged-in admin.
     * @param id ID of the user to delete.
     */
    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable("id") long id, RedirectAttributes ra) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usernameLogeado = auth.getName();
            UsuarioEntity usuarioLogeado = usuarioService.findByCorreo(usernameLogeado);

            // Prevent admins from deleting their own account
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

    /* ==================== SERVICE MANAGEMENT ==================== */

    /**
     * Displays all available services to the admin.
     */
    @GetMapping("/servicios")
    public String gestionarServicios(Model model) {
        model.addAttribute("title", "Gestión de Servicios (Admin)");
        model.addAttribute("urlRegister", "/Admins/servicios/crear");

        List<ServicioEntity> lista = servicioService.findAll();
        lista.sort(Comparator.comparing(ServicioEntity::getIdServicio));

        model.addAttribute("Servicios", lista);
        return "Admins/gestionarServicio";
    }

    /**
     * Displays the form to create a new service.
     */
    @GetMapping("/servicios/crear")
    public String crearServicio(Model model) {
        model.addAttribute("title", "Registrar Servicio (Admin)");
        model.addAttribute("servicio", new ServicioEntity());
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        model.addAttribute("proveedores", usuarioService.findAllProveedores());
        return "Admins/crearServicio";
    }

    /**
     * Handles service creation requests.
     * Validates inputs and associates the service with a provider.
     */
    @PostMapping("/servicios/crear")
    public String crearServicio(@Valid @ModelAttribute("servicio") ServicioEntity servicio,
                                BindingResult result,
                                @RequestParam("proveedorNombre") String proveedorNombre,
                                RedirectAttributes ra,
                                Model model) {

        if (result.hasErrors()) {
            // Reload supporting data if validation fails
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            return "Admins/crearServicio";
        }

        // Validate provider name
        String nombreBuscado = proveedorNombre == null ? "" : proveedorNombre.trim();
        if (nombreBuscado.isEmpty()) {
            model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
            model.addAttribute("proveedores", usuarioService.findAllProveedores());
            model.addAttribute("error", "Debes seleccionar un proveedor.");
            return "Admins/crearServicio";
        }

        // Find provider by name (case-insensitive)
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

        // 🔹 Set default state for new services
        servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);

        // Associate provider and save service
        servicio.setProveedor(proveedor);
        servicioService.save(servicio);
        ra.addFlashAttribute("mensajeExito", "Servicio creado correctamente");
        return "redirect:/Admins/servicios";
    }

    /**
     * Displays the edit form for a service by ID.
     */
    @GetMapping("/servicios/{id}/editar")
    public String editarServicio(@PathVariable("id") Long idServicio, Model model) {
        ServicioEntity servicio = servicioService.findById(idServicio);
        model.addAttribute("title", "Editar Servicio (Admin)");
        model.addAttribute("servicio", servicio);
        model.addAttribute("servicioEdit", servicio);
        model.addAttribute("estados", ServicioEntity.EstadoServicio.values());
        return "Admins/editarServicio";
    }

    /**
     * Handles the update of an existing service.
     */
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

    /**
     * Deletes a service by its ID.
     */
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

    // ==================== SERVICE HISTORY (ADMIN) ====================

    /**
     * Displays the historical list of all services.
     * @param model Model used to pass data to the view.
     * @return Path to "historialServicios" view.
     */
    @GetMapping("/servicios/historial")
    public String historialServiciosAdmin(Model model) {
        model.addAttribute("title", "Historial de Servicios");
        List<ServicioEntity> lista = servicioService.findAll();
        lista.sort(Comparator.comparing(ServicioEntity::getIdServicio).reversed());
        model.addAttribute("solicitudes", lista); // "solicitudes" is the expected variable name in the view
        return "Solicitud/historialServicios";
    }
}
