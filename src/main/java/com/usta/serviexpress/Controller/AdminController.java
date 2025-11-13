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
 *
 * Purpose:
 * - Comprehensive administrative controller handling user management, service management, and service history viewing.
 * - Provides CRUD operations for both users (UsuarioEntity) and services (ServicioEntity) within the admin module.
 * - Implements security measures to prevent self-deletion of admin accounts.
 * - Serves as the central administrative interface for the ServiceExpress application.
 *
 * Routing:
 * - All endpoints are prefixed with "/Admins" to separate admin functionality from regular user operations.
 * - Uses Thymeleaf templates for server-side rendering of administrative views.
 *
 * Security considerations:
 * - Prevents administrators from deleting their own accounts while logged in
 * - Uses Spring Security context for authentication and authorization
 * - Validates provider assignments during service creation
 *
 * Key features:
 * - User management with role assignment
 * - Service lifecycle management with state tracking
 * - Provider-service association
 * - Service history and audit trail
 */
@Controller
@RequestMapping("/Admins")
public class AdminController {

    // ==================== DEPENDENCY INJECTIONS ====================

    @Autowired private UsuarioService usuarioService;  // Service for user entity operations
    @Autowired private ServicioService servicioService; // Service for service entity operations
    @Autowired private RolService rolService;           // Service for role management operations

    /* ==================== USER MANAGEMENT ==================== */

    /**
     * Displays the user management dashboard for administrators.
     *
     * @param model Spring Model for passing user list and view metadata
     * @return String view name for the user management template
     *
     * Data processing:
     * - Retrieves all users from the service layer
     * - Sorts users by ID for consistent display order
     * - Provides URL for user creation functionality
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
     * Displays the user creation form for administrators.
     *
     * @param model Spring Model for passing form data and role options
     * @return String view name for the user creation form template
     *
     * Preparation:
     * - Initializes new UserEntity for form binding
     * - Loads all available roles for assignment dropdown
     */
    @GetMapping("/usuarios/crear")
    public String crearUsuarioUsuariosCrear(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("roles", rolService.findAll()); // Provide available roles
        return "Admins/crearUsuario";
    }

    /**
     * Processes user creation form submission from administrators.
     *
     * @param usuario UserEntity with form data bound from creation form
     * @param result BindingResult for form validation errors
     * @param ra RedirectAttributes for flash messages on redirect
     * @return String redirect to user management on success, or back to form on error
     *
     * Validation:
     * - Returns to form if validation errors exist
     * - Saves user and provides success feedback on successful creation
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
     * Displays the user editing form for a specific user.
     *
     * @param idUsuario Long ID of the user to edit from path variable
     * @param model Spring Model for passing user data and role options to view
     * @return String view name for the user edit template
     *
     * Data loading:
     * - Retrieves existing user entity by ID
     * - Loads all available roles for assignment dropdown
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
     * Processes user editing form submission from administrators.
     *
     * @param usuario UserEntity with updated form data
     * @param idUsuario Long ID of the user being edited from path variable
     * @param result BindingResult for form validation errors
     * @param ra RedirectAttributes for flash messages on redirect
     * @return String redirect to user management on success, or back to form on error
     *
     * Update strategy:
     * - Retrieves existing user entity from database
     * - Updates all user fields from the form data
     * - Preserves the existing entity reference for proper JPA update
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
     * Handles user deletion requests from administrators with security protection.
     *
     * @param id Long ID of the user to delete from path variable
     * @param ra RedirectAttributes for success/error flash messages
     * @return String redirect to user management after deletion attempt
     *
     * Security protection:
     * - Prevents administrators from deleting their own accounts while logged in
     * - Uses Spring Security context to identify currently logged-in user
     * - Provides clear error message when self-deletion is attempted
     *
     * Error handling:
     * - Catches exceptions during deletion and provides user-friendly error messages
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
     * Displays the service management dashboard for administrators.
     *
     * @param model Spring Model for passing service list and view metadata
     * @return String view name for the service management template
     *
     * Data processing:
     * - Retrieves all services from the service layer
     * - Sorts services by ID for consistent display order
     * - Provides URL for service creation functionality
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
     * Displays the service creation form for administrators.
     *
     * @param model Spring Model for passing form data and dropdown options
     * @return String view name for the service creation form template
     *
     * Preparation:
     * - Initializes new ServiceEntity for form binding
     * - Loads all available service states from enum
     * - Loads all available providers for assignment dropdown
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
     * Processes service creation form submission from administrators.
     *
     * @param servicio ServiceEntity with form data bound from creation form
     * @param result BindingResult for form validation errors
     * @param proveedorNombre String name of the selected provider from form
     * @param ra RedirectAttributes for flash messages on redirect
     * @param model Spring Model for passing attributes back to form on error
     * @return String redirect to service management on success, or back to form on error
     *
     * Provider assignment:
     * - Validates provider selection from dropdown
     * - Performs case-insensitive matching to find provider by name
     * - Sets default service state to "DISPONIBLE" (AVAILABLE)
     * - Associates service with the selected provider
     *
     * Error handling:
     * - Returns to form with error messages if provider validation fails
     * - Reloads dropdown options when validation errors occur
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

        // Set default state for new services
        servicio.setEstado(ServicioEntity.EstadoServicio.DISPONIBLE);

        // Associate provider and save service
        servicio.setProveedor(proveedor);
        servicioService.save(servicio);
        ra.addFlashAttribute("mensajeExito", "Servicio creado correctamente");
        return "redirect:/Admins/servicios";
    }

    /**
     * Displays the service editing form for a specific service.
     *
     * @param idServicio Long ID of the service to edit from path variable
     * @param model Spring Model for passing service data and state options to view
     * @return String view name for the service edit template
     *
     * Data loading:
     * - Retrieves existing service entity by ID
     * - Loads all available service states from enum for dropdown
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
     * Processes service editing form submission from administrators.
     *
     * @param servicio ServiceEntity with updated form data
     * @param idServicio Long ID of the service being edited from path variable
     * @param result BindingResult for form validation errors
     * @param ra RedirectAttributes for flash messages on redirect
     * @return String redirect to service management on success, or back to form on error
     *
     * Update strategy:
     * - Retrieves existing service entity from database
     * - Updates core service fields (name, description, price, state)
     * - Preserves the existing entity reference for proper JPA update
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
     * Handles service deletion requests from administrators.
     *
     * @param id Long ID of the service to delete from path variable
     * @param ra RedirectAttributes for success/error flash messages
     * @return String redirect to service management after deletion attempt
     *
     * Error handling:
     * - Catches exceptions during deletion and provides user-friendly error messages
     * - Uses try-catch to handle potential database constraint violations
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
     * Displays the complete service history for administrative review.
     *
     * @param model Spring Model for passing service history data to view
     * @return String view name for the service history template
     *
     * Data processing:
     * - Retrieves all services from the service layer
     * - Sorts services by ID in descending order (most recent first)
     * - Uses "solicitudes" as the model attribute name expected by the view template
     *
     * Note:
     * - Shares the same view template as regular user service history
     * - Provides administrators with comprehensive audit trail of all services
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

/*
Summary (Technical Note):
AdminController is a comprehensive Spring MVC controller that provides administrative functionality for the 
ServiceExpress application. It handles user management (CRUD operations with role assignment), service management 
(creation, editing, deletion with provider association), and service history viewing. The controller implements 
important security measures including prevention of self-deletion for logged-in administrators. It uses Spring 
Security for authentication context and provides robust form validation with user-friendly error handling. Service 
management includes provider assignment validation and default state setting. The controller serves as the central 
administrative interface, separating admin operations from regular user functionality through the "/Admins" URL prefix.
*/
