package com.usta.serviexpress.Controller;

import com.usta.serviexpress.Entity.RolEntity;
import com.usta.serviexpress.Entity.UsuarioEntity;
import com.usta.serviexpress.Service.RolService;
import com.usta.serviexpress.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

/**
 * UsuarioController
 *
 * Purpose:
 * - Spring MVC Controller handling user management operations including registration, CRUD operations, and role assignment.
 * - Provides public user registration and administrative user management functionality.
 * - Integrates with Spring Security for password encoding and user authentication.
 *
 * Key responsibilities:
 * - Public user registration with password confirmation and role assignment
 * - Administrative CRUD operations for user management
 * - Role-based access control (assigns CLIENT role by default for registrations)
 * - Form validation and error handling
 *
 * Security considerations:
 * - Uses BCrypt password encoding for secure password storage
 * - Validates password confirmation during registration
 * - Assigns appropriate roles to prevent privilege escalation
 *
 * Thymeleaf integration:
 * - Returns view names for Thymeleaf template resolution
 * - Uses Model to pass data to views
 * - Implements PRG (Post-Redirect-Get) pattern for form submissions
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;

    /**
     * Displays the public user registration form.
     *
     * @param model Spring Model for passing attributes to the view
     * @return String view name for the registration form template
     *
     * Purpose:
     * - Prepares and displays the user registration form for public access
     * - Initializes a new UserEntity for form binding
     */
    @GetMapping("/register")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("title", "Registro de Usuario");
        return "register";
    }

    /**
     * Processes user registration form submission.
     *
     * @param usuario UserEntity with form data bound from the registration form
     * @param result BindingResult for form validation errors
     * @param confirmarClave Password confirmation field from the form
     * @param model Spring Model for passing attributes to the view
     * @param redirectAttributes Flash attributes for redirect scenarios
     * @param status SessionStatus to mark session as complete
     * @return String redirect to login page on success, or back to form on error
     *
     * Validation steps:
     * 1. Form field validation (annotations on UserEntity)
     * 2. Password confirmation matching
     * 3. Optional: Email uniqueness check (commented out)
     *
     * Security processing:
     * - Encodes plain text password using BCrypt
     * - Assigns CLIENT role (ID: 3) to new users
     * - Throws exception if CLIENT role is missing from database
     */
    @PostMapping("/register")
    public String registrarUsuario(@ModelAttribute("usuario") @Valid UsuarioEntity usuario,
                                   BindingResult result,
                                   @RequestParam("confirmarClave") String confirmarClave,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   SessionStatus status) {

        // Basic form validation
        if (result.hasErrors()) {
            model.addAttribute("title", "Registro de Usuario");
            return "register";
        }

        // Password confirmation validation
        if (usuario.getClave() == null || confirmarClave == null || !usuario.getClave().equals(confirmarClave)) {
            result.rejectValue("clave", "error.usuario", "Las contraseñas no coinciden.");
            model.addAttribute("title", "Registro de Usuario");
            return "register";
        }

        // 1) Encode password for secure storage
        usuario.setClave(new BCryptPasswordEncoder().encode(usuario.getClave()));

        // 2) Assign CLIENT role (id = 3) from service
        RolEntity rolCliente = rolService.findById(3L);
        if (rolCliente == null) {
            // Protection in case someone deleted the CLIENT role from the table
            throw new IllegalStateException("No existe el rol CLIENTE (id=3) en la tabla roles");
        }
        usuario.setRol(rolCliente);

        // 3) Save user
        usuarioService.save(usuario);
        status.setComplete();
        redirectAttributes.addFlashAttribute("success", "¡Usuario registrado correctamente!");
        return "redirect:/login";
    }

    /**
     * Displays the user management list for administrators.
     *
     * @param model Spring Model for passing user list and metadata to view
     * @return String view name for the user list template
     *
     * Data processing:
     * - Retrieves all users from service
     * - Sorts users by ID for consistent display order
     * - Adds URL for "new user" button functionality
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("title", "Gestionar Usuarios");
        model.addAttribute("urlRegisterUser", "/usuarios/crear"); // "new" button URL
        List<UsuarioEntity> lista = usuarioService.findAll();
        lista.sort(Comparator.comparing(UsuarioEntity::getIdUsuario));
        model.addAttribute("Usuarios", lista);
        return "Administrador/ListarUsuario";
    }

    /**
     * Displays the user creation form for administrators.
     *
     * @param model Spring Model for passing form data and role list
     * @return String view name for the user form template
     *
     * Preparation:
     * - Initializes new UserEntity for form binding
     * - Loads all available roles for role selection dropdown
     */
    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("title", "Nuevo Usuario");
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("listaRoles", rolService.findAll());
        return "Administrador/formUsuario";
    }

    /**
     * Processes user creation form submission from administrators.
     *
     * @param usuario UserEntity with form data bound from creation form
     * @param result BindingResult for form validation errors
     * @param ra RedirectAttributes for flash messages on redirect
     * @param model Spring Model for passing attributes back to form on error
     * @return String redirect to user list on success, or back to form on error
     *
     * Role assignment:
     * - Falls back to CLIENT role (ID: 3) if no role is selected in form
     * - Ensures all users have at least a default role assignment
     */
    @PostMapping("/crear")
    public String crear(@Valid @ModelAttribute("usuario") UsuarioEntity usuario,
                        BindingResult result,
                        RedirectAttributes ra,
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaRoles", rolService.findAll());
            return "Administrador/formUsuario";
        }
        // If no role was selected in form, force CLIENT role
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            RolEntity rolCliente = rolService.findById(3L);
            if (rolCliente == null) throw new IllegalStateException("Falta rol CLIENTE (id=3)");
            usuario.setRol(rolCliente);
        }
        usuarioService.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    /**
     * Displays the user editing form for administrators.
     *
     * @param idUsuario Long ID of the user to edit from path variable
     * @param model Spring Model for passing user data and role list to view
     * @return String view name for the user edit template, or redirect if user not found
     *
     * Error handling:
     * - Redirects to user list if specified user ID doesn't exist
     * - Loads all roles for role selection dropdown in edit form
     */
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable("id") Long idUsuario, Model model) {
        UsuarioEntity usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            return "redirect:/usuarios";
        }
        model.addAttribute("title", "Editar Usuario");
        model.addAttribute("usuarioEdit", usuario);
        model.addAttribute("listaRoles", rolService.findAll());
        return "Administrador/editarUsuario";
    }

    /**
     * Processes user editing form submission from administrators.
     *
     * @param usuario UserEntity with updated form data
     * @param result BindingResult for form validation errors
     * @param idUsuario Long ID of the user being edited from path variable
     * @param ra RedirectAttributes for flash messages on redirect
     * @param model Spring Model for passing attributes back to form on error
     * @return String redirect to user list on success, or back to form on error
     *
     * Update strategy:
     * - Retrieves existing user entity from database
     * - Updates individual fields from the form data
     * - Preserves the existing entity reference for proper JPA update
     */
    @PostMapping("/{id}/editar")
    public String editar(@Valid @ModelAttribute("usuarioEdit") UsuarioEntity usuario,
                         BindingResult result,
                         @PathVariable("id") Long idUsuario,
                         RedirectAttributes ra,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaRoles", rolService.findAll());
            model.addAttribute("title", "Editar Usuario");
            return "Administrador/editarUsuario";
        }
        UsuarioEntity existente = usuarioService.findById(idUsuario);
        if (existente == null) {
            ra.addFlashAttribute("errorMensaje", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        existente.setCorreo(usuario.getCorreo());
        existente.setClave(usuario.getClave());
        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setRol(usuario.getRol());
        usuarioService.save(existente);

        ra.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente");
        return "redirect:/usuarios";
    }

    /**
     * Handles user deletion requests from administrators.
     *
     * @param id Long ID of the user to delete from path variable
     * @param ra RedirectAttributes for success/error flash messages
     * @return String redirect to user list after deletion attempt
     *
     * Error handling:
     * - Checks if user exists before attempting deletion
     * - Catches exceptions during deletion and provides user-friendly error messages
     * - Uses try-catch to handle potential database constraint violations
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") long id, RedirectAttributes ra) {
        UsuarioEntity usuario = usuarioService.findById(id);
        if (usuario == null) {
            ra.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        try {
            usuarioService.deleteById(id);
            ra.addFlashAttribute("success", "Usuario eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}

/*
Summary (Technical Note):
UsuarioController is a Spring MVC controller that manages user-related operations for a ServiceExpress application.
It provides both public user registration functionality and administrative user management capabilities. The controller
handles user creation, reading, updating, and deletion (CRUD) with proper form validation, password encoding using
BCrypt, and role-based access control. Public registrations automatically assign the CLIENT role (ID: 3), while
administrative functions allow role assignment and full user management. The controller implements the PRG pattern,
uses Thymeleaf templates for views, and provides comprehensive error handling with user-friendly flash messages.
Security considerations include password confirmation validation and proper role assignment to prevent privilege issues.
*/
